package com.pmp.enumeration;
import lombok.Getter;

/**
 * 任务申请审核状态枚举
 */
@Getter
public enum ApplicationStatus {
    /**
     * 待审核
     */
    PENDING("待审核"),

    /**
     * 已通过（已自动分配）
     */
    APPROVED("已通过"),

    /**
     * 已拒绝
     */
    REJECTED("已拒绝");

    private final String label;

    ApplicationStatus(String label) {
        this.label = label;
    }
}
