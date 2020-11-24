package com.fei.food.rewardservicemanager.po;

import java.math.BigDecimal;
import java.util.Date;
import com.fei.food.rewardservicemanager.ennumeration.RewardStatus;
import lombok.Data;

@Data
public class RewardPO {
    private Integer id;
    private Integer orderId;
    private BigDecimal amount;
    private RewardStatus status;
    private Date date;
}
