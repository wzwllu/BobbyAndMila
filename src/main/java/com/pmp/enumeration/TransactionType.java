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
    EARN("赚取"),

    /**
     * 消耗积分
     */
    CONSUME("消耗");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }
}
