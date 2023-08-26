package com.education.orders.service.impl;

import com.education.orders.model.dto.AddOrderDto;
import com.education.orders.model.dto.PayRecordDto;
import com.education.orders.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yang
 * @create 2023-08-26 17:38
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Override
    @Transactional
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {

        return null;
    }

}
