package com.fei.food.orderServiceManagerApplication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fei.food.orderServiceManagerApplication.dao.OrderDetailDao;
import com.fei.food.orderServiceManagerApplication.dto.OrderMessageDTO;
import com.fei.food.orderServiceManagerApplication.enummeration.OrderStatus;
import com.fei.food.orderServiceManagerApplication.po.OrderDetailPO;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 消息处理相关业务逻辑
 */
@Slf4j
@Service
public class OrderMessageService {

    @Autowired
    OrderDetailDao orderDetailDao;

    //序列化的工具，将对象转换成字符或字节
    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 声明消息队列、交换机、绑定、消息的处理
     *
     * @throws IOException
     * @throws TimeoutException
     * @throws InterruptedException
     * @Async标志这是个异步线程 注意：设置异步线程，一定要设置线程池，否则会引来线程爆炸
     */
    @Async
    public void handleMessage() throws IOException, TimeoutException, InterruptedException {
        //Java连接上RabbitMQ
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        //包在try里面，会自动关闭连接，就不需要在finally里手动写connection.close()，
        // 是因为Connection这个类实现了AutoCloseable接口，就可以使用这种java的写法
        try (Connection connection = connectionFactory.newConnection();
             //和rabbitmq通信，用的还是这个channel
             Channel channel = connection.createChannel()) {

            //在这里每两个微服务之间通信，使用单独的Exchange，方便理解
            /*---------------------restaurant---------------------*/
            //订单微服务和餐厅微服务交换消息用的交换机
            //第一个参数：交换机名称；第2个：交换机类型；第3个durable：是否需要持久化，一般是需要持久化，不然rabbitmq重启的话，交换机和队列就没了
            //第4个参数autoDelete: 如果没有队列需要这个交换机了，会自动把这个交换机删掉，一般设置为false
            channel.exchangeDeclare("exchange.order.restaurant", BuiltinExchangeType.DIRECT, true, false, null);

            //exclusive:表示是否声明一个独占队列，即这个队列是不是这个connection独占的，有时候想让多个应用都可以监听这个队列，所以设置为false
            channel.queueDeclare("queue.order", true, false, false, null);

            channel.queueBind("queue.order", "exchange.order.restaurant", "key.order");

            /*---------------------deliveryman---------------------*/
            channel.exchangeDeclare("exchange.order.deliveryman", BuiltinExchangeType.DIRECT, true, false, null);

            channel.queueBind("queue.order", "exchange.order.deliveryman", "key.order");

            /*---------------------settlement---------------------*/
            channel.exchangeDeclare("exchange.order.settlement", BuiltinExchangeType.FANOUT, true, false, null);

            channel.queueBind("queue.order", "exchange.settlement.order", "key.order");

            /*------------------------reward---------------------*/
            channel.exchangeDeclare("exchange.order.reward", BuiltinExchangeType.TOPIC, true, false, null);

            channel.queueBind("queue.order", "exchange.order.reward", "key.order");


            //第2个参数autoAck的意思是，是否自动确认消息，设置为true
            //consumerTag就是消费者的一个标签
            channel.basicConsume("queue.order", true, deliverCallback, consumerTag -> {
            });
            //启一个消费者，然后注册消费的回调，但是注册好之后，不能让这个线程退出，这个线程一旦退出，注册的消费者就没了，所以让它sleeo下
            while (true) {
                Thread.sleep(100000);
            }

        }
    }

    //怎么处理我们收到的消息
    DeliverCallback deliverCallback = (consumerTag, message) -> {

        //new String()将一个字符数组转换成字符串
        String messageBody = new String(message.getBody());

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try {
            //序列化的时候可能会抛出异常，所以放在try块里
            //将消息反序列化成DTO
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody, OrderMessageDTO.class);
            //数据库中读取订单PO
            OrderDetailPO orderPO = orderDetailDao.selectOrder(orderMessageDTO.getOrderId());

            switch (orderPO.getStatus()) {

                case ORDER_CREATING:
                    if (orderMessageDTO.getConfirmed() && null != orderMessageDTO.getPrice()) {
                        orderPO.setStatus(OrderStatus.RESTAURANT_CONFIRMED);
                        orderPO.setPrice(orderMessageDTO.getPrice());
                        orderDetailDao.update(orderPO);

                        //给骑手微服务发送消息
                        try (Connection connection = connectionFactory.newConnection();
                             Channel channel = connection.createChannel()) {
                            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                            channel.basicPublish("exchange.order.deliveryman", "key.deliveryman", null, messageToSend.getBytes());
                        }
                    } else {
                        orderPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderPO);
                    }
                    break;
                case RESTAURANT_CONFIRMED:
                    if (null != orderMessageDTO.getDeliverymanId()) {
                        orderPO.setStatus(OrderStatus.RESTAURANT_CONFIRMED);
                        orderPO.setDeliverymanId(orderMessageDTO.getDeliverymanId());
                        orderDetailDao.update(orderPO);
                        try (Connection connection = connectionFactory.newConnection();
                             Channel channel = connection.createChannel()) {
                            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                            channel.basicPublish("exchange.order.settlement", "key.settlement", null, messageToSend.getBytes());
                        }
                    } else {
                        orderPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderPO);
                    }
                    break;
                case DELIVERYMAN_CONFIRMED:
                    if (null != orderMessageDTO.getSettlementId()) {
                        orderPO.setStatus(OrderStatus.SETTLEMENT_CONFIRMED);
                        orderPO.setSettlementId(orderMessageDTO.getSettlementId());
                        orderDetailDao.update(orderPO);
                        try (Connection connection = connectionFactory.newConnection();
                             Channel channel = connection.createChannel()) {
                            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                            channel.basicPublish("exchange.order.reward", "key.reward", null, messageToSend.getBytes());
                        }
                    } else {
                        orderPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderPO);
                    }
                    break;
                case SETTLEMENT_CONFIRMED:
                    if (null != orderMessageDTO.getRewardId()) {
                        orderPO.setStatus(OrderStatus.ORDER_CREATED);
                        orderPO.setRewardId(orderMessageDTO.getRewardId());
                        orderDetailDao.update(orderPO);
                    } else {
                        orderPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderPO);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + orderPO.getStatus());
            }


        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    };


}
