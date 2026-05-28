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
    NONE,
    
    /**
     * 每日重复
     */
    DAILY,
    
    /**
     * 每周重复
     */
    WEEKLY
}
