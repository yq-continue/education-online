package com.education.orders.service;

import com.education.messagesdk.model.po.MqMessage;
import com.education.orders.model.dto.AddOrderDto;
import com.education.orders.model.dto.PayRecordDto;
import com.education.orders.model.po.XcPayRecord;

/**
 * @author yang
 * @create 2023-08-26 17:38
 */
public interface OrderService {
    /**
     *生成支付二维码
     * @param userId
     * @param addOrderDto
     * @return
     */
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * 查询支付交易记录
     * @param payNo
     * @return
     */
    public XcPayRecord getPayRecordByPayno(String payNo);

    /**
     * 查看订单支付状态
     * @param payNo
     * @return
     */
    public PayRecordDto queryResult(String payNo);

    /**
     * 支付成功后发送支付结果通知
     * @param message
     */
    public void notifyPayResult(MqMessage message);


}
