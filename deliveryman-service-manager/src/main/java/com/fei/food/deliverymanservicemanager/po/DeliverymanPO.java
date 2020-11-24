package com.fei.food.deliverymanservicemanager.po;

import com.fei.food.deliverymanservicemanager.enummeration.DeliverymanStatus;
import lombok.Data;
import java.util.Date;

@Data
public class DeliverymanPO {
    private Integer id;
    private String name;
    private String district;
    private DeliverymanStatus status;
    private Date date;
}
