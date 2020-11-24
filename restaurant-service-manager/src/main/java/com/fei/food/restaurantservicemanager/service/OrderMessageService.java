package com.fei.food.restaurantservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fei.food.restaurantservicemanager.dao.ProductDao;
import com.fei.food.restaurantservicemanager.dao.RestaurantDao;
import com.fei.food.restaurantservicemanager.dto.OrderMessageDTO;
import com.fei.food.restaurantservicemanager.enummeration.ProductStatus;
import com.fei.food.restaurantservicemanager.enummeration.RestaurantStatus;
import com.fei.food.restaurantservicemanager.po.ProductPO;
import com.fei.food.restaurantservicemanager.po.RestaurantPO;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class OrderMessageService {

    @Autowired
    ProductDao productDao;

    @Autowired
    RestaurantDao restaurantDao;

    ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public void handleMessage() throws IOException, TimeoutException, InterruptedException {
        log.info("start linstening message");
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare("exchange.order.restaurant", BuiltinExchangeType.DIRECT, true, false, null);

            channel.queueDeclare("queue.restaurant", true, false, false, null);

            channel.queueBind("queue.restaurant", "exchange.order.restaurant", "key.restaurant");

            channel.basicConsume("queue.restaurant", true, deliverCallback, consumerTag -> {
            });
            while (true) {
                Thread.sleep(100000);
            }
        }
    }

    DeliverCallback deliverCallback = (consumerTag, message) -> {
        //new String()将一个字符数组转换成字符串
        String messageBody = new String(message.getBody());

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            //将消息反序列化成DTO
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody, OrderMessageDTO.class);
            //数据库中读取商品PO
            ProductPO productPO = productDao.selectProduct(orderMessageDTO.getProductId());
            //数据库中读取商家PO
            RestaurantPO restaurantPO = restaurantDao.selectRestaurant(productPO.getRestaurantId());

            if (ProductStatus.AVALIABLE == productPO.getStatus() && RestaurantStatus.OPEN == restaurantPO.getStatus()) {
                orderMessageDTO.setConfirmed(true);
                orderMessageDTO.setPrice(productPO.getPrice());
            } else {
                orderMessageDTO.setConfirmed(false);
            }

            try (Connection connection = connectionFactory.newConnection();
                 Channel channel = connection.createChannel()) {
                String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                channel.basicPublish("exchange.order.restaurant", "key.order", null, messageBody.getBytes());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    };
}
