package com.pmp.enumeration;

import lombok.Getter;

/**
 * 交易类型枚举
 */
@Getter
public enum TransactionType {
    /**
     * 获得积分
     */
    EARN,
    
    /**
     * 消耗积分
     */
    CONSUME
}
