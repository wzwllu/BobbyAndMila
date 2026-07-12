package com.pmp.dto.redemption;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RedemptionResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long rewardId;
    private String rewardName;
    private Integer costPoints;
    private Integer quantity;
    private Integer totalPoints;
    private String remark;
    private String status;
    private String statusLabel;
    private LocalDateTime usedAt;
    private String reviewRemark;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
}
