package com.pmp.dto;

import lombok.Data;

@Data
public class AssignmentRequest {
    private Long projectId;
    private Long workerId;
    private String assignDate;
}
