package com.pmp.dto;

import lombok.Data;

/**
 * 管理员调整积分结果
 */
@Data
public class PointsAdjustResponse {
    private Long userId;
    private String username;
    private Integer adjustAmount;
    private Long newBalance;
    private String message;
}