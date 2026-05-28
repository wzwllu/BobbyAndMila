package com.pmp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignmentRequest {
    private Long projectId;
    private Long workerId;
    private LocalDate assignDate;
}
