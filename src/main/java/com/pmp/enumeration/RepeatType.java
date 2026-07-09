package com.pmp.enumeration;

import lombok.Getter;

/**
 * 重复类型枚举
 */
@Getter
public enum RepeatType {
    /**
     * 不重复
     */
    NONE("不重复"),

    /**
     * 每日重复
     */
    DAILY("每日"),

    /**
     * 每周重复
     */
    WEEKLY("每周");

    private final String label;

    RepeatType(String label) {
        this.label = label;
    }
}
