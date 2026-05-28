package com.pmp.dto;

import lombok.Data;

@Data
public class AssignmentResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long workerId;
    private String workerName;
    private String assignDate;
    private String status;
    private Integer completedQuantity;
}
