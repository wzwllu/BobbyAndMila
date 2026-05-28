package com.pmp.dto;

import lombok.Data;

@Data
public class PointsConsumeRequest {
    private Long projectId;
    private Integer quantity;
}
