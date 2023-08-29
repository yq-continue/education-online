package com.education.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.base.exception.EducationException;
import com.education.base.utils.IdWorkerUtils;
import com.education.base.utils.QRCodeUtil;
import com.education.messagesdk.model.po.MqMessage;
import com.education.messagesdk.service.MqMessageService;
import com.education.orders.config.PayNotifyConfig;
import com.education.orders.mapper.XcOrdersGoodsMapper;
import com.education.orders.mapper.XcOrdersMapper;
import com.education.orders.mapper.XcPayRecordMapper;
import com.education.orders.model.dto.AddOrderDto;
import com.education.orders.model.dto.PayRecordDto;
import com.education.orders.model.po.XcOrders;
import com.education.orders.model.po.XcOrdersGoods;
import com.education.orders.model.po.XcPayRecord;
import com.education.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yang
 * @create 2023-08-26 17:38
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private XcOrdersMapper ordersMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private XcOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    private XcPayRecordMapper payRecordMapper;

    @Autowired
    private MqMessageService mqMessageService;

    @Value("${pay.qrcodeurl}")
    private String qrcodeurl;

    @Override
    @Transactional
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        //插入订单表,订单主表，订单明细表
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);

        //插入支付记录
        XcPayRecord payRecord = createPayRecord(xcOrders);
        Long payNo = payRecord.getPayNo();

        //生成二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        //支付二维码的url
        String url = String.format(qrcodeurl, payNo);
        //二维码图片
        String qrCode = null;
        try {
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        } catch (IOException e) {
            EducationException.cast("生成二维码出错");
        }

        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord,payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        XcPayRecord xcPayRecord = payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
        return xcPayRecord;

    }

    @Override
    @Transactional
    public PayRecordDto queryResult(String payNo) {
        //更新支付记录表中的状态
        LambdaQueryWrapper<XcPayRecord> wrapperOfPayRecord = new LambdaQueryWrapper<>();
        wrapperOfPayRecord.eq(XcPayRecord::getPayNo, payNo);
        XcPayRecord xcPayRecord = payRecordMapper.selectOne(wrapperOfPayRecord);
        if (xcPayRecord == null){
            EducationException.cast("此订单不存在请刷新二维码重新下单");
        }
        xcPayRecord.setOutPayChannel("Alipay");
        xcPayRecord.setStatus("601002");
        xcPayRecord.setPaySuccessTime(LocalDateTime.now());
        payRecordMapper.updateById(xcPayRecord);
        //更新订单表中的状态
        Long orderId = xcPayRecord.getOrderId();
        XcOrders xcOrders = new XcOrders();
        xcOrders.setStatus("600002");
        xcOrders.setId(orderId);
        ordersMapper.updateById(xcOrders);
        //通知消息
        //将支付结果发送给学习服务  参数1：支付结果通知类型，2: 业务id，3:业务类型
        XcOrders orders = ordersMapper.selectById(orderId);
        MqMessage mqMessage = mqMessageService.addMessage("payresult_notify",
                orders.getOutBusinessId(), orders.getOrderType(), null);
        notifyPayResult(mqMessage);
        //返回结果
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(xcPayRecord,payRecordDto);
        return payRecordDto;
    }

    @Override
    public void notifyPayResult(MqMessage message) {
        //1、消息体，转json
        String msg = JSON.toJSONString(message);
        //设置消息持久化
        Message msgObj = MessageBuilder.withBody(msg.getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        // 2.全局唯一的消息ID，需要封装到CorrelationData中
        CorrelationData correlationData = new CorrelationData(message.getId().toString());
        // 3.添加 callback
        correlationData.getFuture().addCallback(
                result -> {
                    if(result.isAck()){
                        // ack，消息成功
                        log.debug("通知支付结果消息发送成功, ID:{}", correlationData.getId());
                        //删除消息表中的记录
                        mqMessageService.completed(message.getId());
                    }else{
                        // nack，消息失败
                        log.error("通知支付结果消息发送失败, ID:{}, 原因{}",correlationData.getId(), result.getReason());
                    }
                },
                ex -> log.error("消息发送异常, ID:{}, 原因{}",correlationData.getId(),ex.getMessage())
        );
        // 发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", msgObj,correlationData);
    }

    /**
     * 保存支付记录
     * @param orders
     * @return
     */
    public XcPayRecord createPayRecord(XcOrders orders){
        //订单id
        Long orderId = orders.getId();
        XcOrders xcOrders = ordersMapper.selectById(orderId);
        //如果此订单不存在不能添加支付记录
        if(xcOrders == null){
            EducationException.cast("订单不存在");
        }
        //订单状态
        String status = xcOrders.getStatus();
        //如果此订单支付结果为成功，不再添加支付记录，避免重复支付
        if("601002".equals(status)){//支付成功
            EducationException.cast("此订单已支付");
        }
        //添加支付记录
        XcPayRecord xcPayRecord = new XcPayRecord();
        xcPayRecord.setPayNo(IdWorkerUtils.getInstance().nextId());//支付记录号，将来要传给支付宝
        xcPayRecord.setOrderId(orderId);
        xcPayRecord.setOrderName(xcOrders.getOrderName());
        xcPayRecord.setTotalPrice(xcOrders.getTotalPrice());
        xcPayRecord.setCurrency("CNY");
        xcPayRecord.setCreateDate(LocalDateTime.now());
        xcPayRecord.setStatus("601001");//未支付
        xcPayRecord.setUserId(xcOrders.getUserId());
        int insert = payRecordMapper.insert(xcPayRecord);
        if(insert<=0){
            EducationException.cast("插入支付记录失败");
        }
        return xcPayRecord;
    }

    /**
     * 保存订单信息
     * @param userId
     * @param addOrderDto
     * @return
     */
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto){
        //插入订单表,订单主表，订单明细表
        //进行幂等性判断，同一个选课记录只能有一个订单
        XcOrders xcOrders = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if(xcOrders!=null){
            return xcOrders;
        }

        //插入订单主表
        xcOrders = new XcOrders();
        //使用雪花算法生成订单号
        xcOrders.setId(IdWorkerUtils.getInstance().nextId());
        xcOrders.setTotalPrice(addOrderDto.getTotalPrice());
        xcOrders.setCreateDate(LocalDateTime.now());
        xcOrders.setStatus("600001");//未支付
        xcOrders.setUserId(userId);
        xcOrders.setOrderType("60201");//订单类型
        xcOrders.setOrderName(addOrderDto.getOrderName());
        xcOrders.setOrderDescrip(addOrderDto.getOrderDescrip());
        xcOrders.setOrderDetail(addOrderDto.getOrderDetail());
        xcOrders.setOutBusinessId(addOrderDto.getOutBusinessId());//如果是选课这里记录选课表的id
        int insert = ordersMapper.insert(xcOrders);
        if(insert<=0){
            EducationException.cast("添加订单失败");
        }
        //订单id
        Long orderId = xcOrders.getId();
        //插入订单明细表
        //将前端传入的明细json串转成List
        String orderDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        //遍历xcOrdersGoods插入订单明细表
        xcOrdersGoods.forEach(goods->{
            goods.setOrderId(orderId);
            //插入订单明细表
            int insert1 = ordersGoodsMapper.insert(goods);
        });
        return xcOrders;
    }


    /**
     * 根据业务id查询订单 ,业务id是选课记录表中的主键
     * @param businessId
     * @return
     */
    public XcOrders getOrderByBusinessId(String businessId) {
        XcOrders orders = ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
        return orders;
    }

}
