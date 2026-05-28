package com.pmp.enumeration;

import lombok.Getter;

/**
 * 项目类型枚举
 */
@Getter
public enum ProjectType {
    /**
     * 基于费率的项目
     */
    RATE_BASED,
    
    /**
     * 基于积分消耗的项目
     */
    POINT_CONSUMING
}
