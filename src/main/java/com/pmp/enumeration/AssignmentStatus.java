package com.pmp.enumeration;

import lombok.Getter;

/**
 * 任务分配状态枚举
 */
@Getter
public enum AssignmentStatus {
    /**
     * 进行中
     */
    ACTIVE("进行中"),

    /**
     * 已完成
     */
    COMPLETED("已完成"),

    /**
     * 已取消
     */
    CANCELLED("已取消");

    private final String label;

    AssignmentStatus(String label) {
        this.label = label;
    }
}
