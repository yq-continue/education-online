package com.education.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.education.base.exception.EducationException;
import com.education.learning.config.PayNotifyConfig;
import com.education.learning.service.MyCourseTablesService;
import com.education.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yang
 * @create 2023-08-29 16:17
 */
@Slf4j
@Service
public class ReceivePayNotifyService {

    @Autowired
    MyCourseTablesService myCourseTablesService;


    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(Message message) {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //解析出消息
        byte[] body = message.getBody();
        String jsonString = new String(body);
        //转成对象
        MqMessage mqMessage = JSON.parseObject(jsonString, MqMessage.class);
        //解析消息的内容
        //选课id
        String chooseCourseId = mqMessage.getBusinessKey1();
        //订单类型
        String orderType = mqMessage.getBusinessKey2();
        //学习中心服务只要购买课程类的支付订单的结果
        if ("60201".equals(orderType)) {
            //根据消息内容，更新选课记录、向我的课程表插入记录
            boolean b = myCourseTablesService.saveChooseCourseSuccess(chooseCourseId);
            if (!b) {
                EducationException.cast("保证选课记录状态失败");
            }
        }
    }
}

