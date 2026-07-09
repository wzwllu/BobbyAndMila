package com.pmp.dto;

import lombok.Data;

@Data
public class TaskStatsResponse {
    private Long projectId;
    private String projectName;
    private String projectType;
    private long completedCount;
    private long incompleteCount;
    private long totalPoints;
}
