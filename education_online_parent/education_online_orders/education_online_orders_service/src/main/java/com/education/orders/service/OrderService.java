package com.education.orders.service;

import com.education.orders.model.dto.AddOrderDto;
import com.education.orders.model.dto.PayRecordDto;

/**
 * @author yang
 * @create 2023-08-26 17:38
 */
public interface OrderService {

    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

}
