package com.fei.food.orderServiceManagerApplication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fei.food.orderServiceManagerApplication.dao.OrderDetailDao;
import com.fei.food.orderServiceManagerApplication.dto.OrderMessageDTO;
import com.fei.food.orderServiceManagerApplication.po.OrderDetailPO;
import com.fei.food.orderServiceManagerApplication.vo.OrderCreateVO;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 处理用户关于订单的业务请求
 */
@Service
public class OrderService {

    @Autowired
    OrderDetailDao orderDetailDao;

    //序列化的工具，将对象转换成字符或字节
    ObjectMapper objectMapper = new ObjectMapper();

    public void createOrder(OrderCreateVO orderCreateVO) throws IOException, TimeoutException {
        OrderDetailPO orderDetailPO = new OrderDetailPO();
        orderDetailPO.setAddress(orderCreateVO.getAddress());
        orderDetailPO.setAccountId(orderCreateVO.getAccountId());
        orderDetailPO.setProductId(orderCreateVO.getProductId());
        orderDetailDao.insert(orderDetailPO);

        OrderMessageDTO orderMessageDTO = new OrderMessageDTO();
        orderMessageDTO.setOrderId(orderDetailPO.getId());
        orderMessageDTO.setAccountId(orderDetailPO.getAccountId());
        orderMessageDTO.setProductId(orderDetailPO.getProductId());

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            //接收和发送的消息都是字符或字节类型的，不是一个对象
            //把dto转换成json对象
            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
            //basicPublish:消息发送。 最后一个参数得用字节，不能用String，RabbitMQ的基础的收发是用字节
            channel.basicPublish("exchange.order.restaurant", "key.restaurant", null, messageToSend.getBytes());


        }


    }
}
