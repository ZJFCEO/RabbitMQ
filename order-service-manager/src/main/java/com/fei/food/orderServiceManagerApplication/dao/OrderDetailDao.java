package com.fei.food.orderServiceManagerApplication.dao;

import com.fei.food.orderServiceManagerApplication.po.OrderDetailPO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface OrderDetailDao {

    /**
     * @Options注解，能够为对象生成自增的主键值
     * @param orderDetailPO
     */
    @Insert("INSERT INTO order_detail (status, address, account_id, product_id, deliveryman_id, settlement_id, " +
            "reward_id, price, date) VALUES(#{status}, #{address},#{accountId},#{productId},#{deliverymanId}," +
            "#{settlementId}, #{rewardId},#{price}, #{date})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(OrderDetailPO orderDetailPO);

    @Update("update order_detail set status =#{status}, address =#{address}, account_id =#{accountId}, " +
            "product_id =#{productId}, deliveryman_id =#{deliverymanId}, settlement_id =#{settlementId}, " +
            "reward_id =#{rewardId}, price =#{price}, date =#{date} where id=#{id}")
    void update(OrderDetailPO orderDetailPO);

    /**
     * select后面的字段先是数据库中的，然后赋值给自己定义的字段，
     * 如果两者字段名相同，写一次就可以，不同的话，先写数据库的字段，再空格，然后自己定义的字段名
     * @param id
     * @return
     */
    @Select("SELECT id,status,address,account_id accountId, product_id productId,deliveryman_id deliverymanId," +
            "settlement_id settlementId,reward_id rewardId,price, date FROM order_detail WHERE id = #{id}")
    OrderDetailPO selectOrder(Integer id);
}
