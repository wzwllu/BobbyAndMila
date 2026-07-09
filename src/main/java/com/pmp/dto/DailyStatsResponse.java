package com.pmp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyStatsResponse {
    private LocalDate date;
    private long completedCount;
    private long earnedPoints;
    private long consumedPoints;
}
