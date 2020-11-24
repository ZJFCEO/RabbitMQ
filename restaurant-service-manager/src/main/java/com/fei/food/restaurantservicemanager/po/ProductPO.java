package com.fei.food.restaurantservicemanager.po;


import com.fei.food.restaurantservicemanager.enummeration.ProductStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ProductPO {
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer restaurantId;
    private ProductStatus status;
    private Date date;
}
