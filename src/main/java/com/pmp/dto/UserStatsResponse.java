package com.pmp.dto;

import lombok.Data;

@Data
public class UserStatsResponse {
    private Long userId;
    private String username;
    private Long completedTasks;
    private Long incompleteTasks;
    private Long totalEarned;
    private Long totalConsumed;
    private Long balance;
}
