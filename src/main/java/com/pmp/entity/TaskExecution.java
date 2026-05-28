package com.pmp.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 任务执行实体类
 */
@Entity
@Table(name = "task_executions")
@Data
public class TaskExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private ProjectAssignment assignment;
    
    @Column(name = "execution_date", nullable = false)
    private LocalDate executionDate;
    
    private Integer quantity;
    
    @Column(name = "points_earned")
    private Integer pointsEarned;
    
    @Column(name = "points_consumed")
    private Integer pointsConsumed;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
