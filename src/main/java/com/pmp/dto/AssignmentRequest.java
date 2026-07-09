package com.pmp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignmentRequest {
    private Long projectId;
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
}
