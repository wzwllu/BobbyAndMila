package com.pmp.dto;

import com.pmp.enumeration.ProjectType;
import com.pmp.enumeration.ProjectStatus;
import com.pmp.enumeration.RepeatType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private ProjectType type;
    private ProjectStatus status;
    private BigDecimal unitPrice;
    private RepeatType repeatType;
    private LocalDate endDate;
    private Integer pointsToConsume;
    private Long createdBy;
    private LocalDateTime createdAt;
}
