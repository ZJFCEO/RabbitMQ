package com.fei.food.settlementservicemanager.enummeration;

public enum OrderStatus {
    /**
     * 订单创建中
     */
    ORDER_CREATING,
    /**
     * 餐厅已确认
     */
    RESTAURANT_CONFIRMED,
    /**
     * 骑手已确认
     */
    DELIVERYMAN_CONFIRMED,
    /**
     * 已结算
     */
    SETTLEMENT_CONFIRMED,
    /**
     * 订单已创建
     */
    ORDER_CREATED,
    /**
     * 订单创建失败
     */
    FAILED;
}
