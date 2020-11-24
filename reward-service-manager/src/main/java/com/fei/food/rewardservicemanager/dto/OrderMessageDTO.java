package com.fei.food.rewardservicemanager.dto;

import java.math.BigDecimal;

import com.fei.food.rewardservicemanager.ennumeration.OrderStatus;
import lombok.Data;


@Data
public class OrderMessageDTO {
    /**
     * 订单ID
     */
    private Integer orderId;
    /**
     * 订单状态
     */
    private OrderStatus orderStatus;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 骑手ID
     */
    private Integer deliverymanId;
    /**
     * 产品ID
     */
    private Integer productId;
    /**
     * 用户ID
     */
    private Integer accountId;
    /**
     * 结算ID
     */
    private Integer settlementId;
    /**
     * 积分结算ID
     */
    private Integer rewardId;
    /**
     * 积分奖励数量
     */
    private Integer rewardAmount;
    /**
     * 确认
     */
    private Boolean confirmed;
}
