package com.pmp.dto;

import com.pmp.enumeration.AssignmentStatus;
import com.pmp.enumeration.ProjectType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AssignmentResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private ProjectType projectType;
    private BigDecimal unitPrice;
    private Integer pointsToConsume;
    private Long userId;
    private String userName;
    private LocalDate startDate;
    private LocalDate endDate;
    private AssignmentStatus status;
    private LocalDateTime createdAt;
}
