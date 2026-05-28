package com.pmp.dto;

import com.pmp.enumeration.AssignmentStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignmentResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long workerId;
    private String workerName;
    private LocalDate assignDate;
    private AssignmentStatus status;
    private Integer completedQuantity;
}
