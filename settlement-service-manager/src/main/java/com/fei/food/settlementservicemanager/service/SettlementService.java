package com.fei.food.settlementservicemanager.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class SettlementService {

    Random rand = new Random(25);

    public Integer settlement(Integer accountId, BigDecimal amount) {
        //这里实际要实现真正的结算业务代码
        //根据业务需要
        return rand.nextInt(1000000000);
    }
}
