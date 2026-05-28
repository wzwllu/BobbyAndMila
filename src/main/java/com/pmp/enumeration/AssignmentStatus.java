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
    ACTIVE,
    
    /**
     * 已完成
     */
    COMPLETED
}
