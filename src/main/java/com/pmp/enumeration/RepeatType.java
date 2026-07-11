package com.pmp.enumeration;

import lombok.Getter;

/**
 * 重复类型枚举
 */
@Getter
public enum RepeatType {
    NONE("不重复"),
    DAILY("每日");

    private final String label;

    RepeatType(String label) {
        this.label = label;
    }
}
