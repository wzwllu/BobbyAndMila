package com.pmp.dto;

import lombok.Data;

/**
 * 管理员调整积分请求
 */
@Data
public class PointsAdjustRequest {
    /** 目标用户ID */
    private Long userId;
    /** 调整积分数（正数增加，负数扣除） */
    private Integer amount;
    /** 调整原因 */
    private String reason;
}