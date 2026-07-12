package com.pmp.dto.reward;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RewardResponse {
    private Long id;
    private String name;
    private Integer costPoints;
    private String description;
    private String imageUrl;
    private Integer stock;
    private String status;
    private String statusLabel;
    private Long createdBy;
    private LocalDateTime createdAt;
}
