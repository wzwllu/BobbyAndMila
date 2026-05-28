package com.pmp.dto;

import lombok.Data;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String type;
    private Double unitPrice;
    private String repeatType;
    private String repeatDay;
    private Integer pointsToConsume;
    private Long createdBy;
    private String createdAt;
}
