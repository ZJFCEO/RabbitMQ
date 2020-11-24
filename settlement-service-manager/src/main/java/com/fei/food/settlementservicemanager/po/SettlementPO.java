package com.fei.food.settlementservicemanager.po;

import com.fei.food.settlementservicemanager.enummeration.SettlementStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class SettlementPO {
    private Integer id;
    private Integer orderId;
    private Integer transactionId;
    private SettlementStatus status;
    private BigDecimal amount;
    private Date date;
}
