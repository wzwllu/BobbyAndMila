package com.pmp.enumeration;
import lombok.Getter;

/**
 * 任务执行审核状态枚举
 */
@Getter
public enum TaskExecutionStatus {
    /**
     * 待审核
     */
    PENDING("待审核"),

    /**
     * 已通过
     */
    APPROVED("已通过"),

    /**
     * 已拒绝
     */
    REJECTED("已拒绝");

    private final String label;

    TaskExecutionStatus(String label) {
        this.label = label;
    }
}
