package com.fei.food.orderServiceManagerApplication.controller;

import com.fei.food.orderServiceManagerApplication.service.OrderService;
import com.fei.food.orderServiceManagerApplication.vo.OrderCreateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
public class OrderControler {

    @Autowired
    OrderService orderService;

    @PostMapping("/orders")
    public void createOrder(@RequestBody OrderCreateVO orderCreateVO) throws IOException, TimeoutException {
        log.info("createOrder: orderCreateVO:{}",orderCreateVO);
        orderService.createOrder(orderCreateVO);
    }
}
