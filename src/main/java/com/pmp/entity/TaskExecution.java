package com.pmp.entity;

import com.pmp.enumeration.TaskExecutionStatus;
import com.pmp.enumeration.TransactionType;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务执行实体类（赚取或消耗积分的任务记录，需审核后入账）
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

    /**
     * 任务方向：EARN 赚取积分 / CONSUME 消耗积分
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(name = "execution_date", nullable = false)
    private LocalDate executionDate;

    private Integer quantity;

    /**
     * 本任务涉及的积分数（赚取或消耗）
     */
    @Column(name = "points")
    private Integer points;

    /**
     * 审核状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskExecutionStatus status = TaskExecutionStatus.PENDING;

    /**
     * 任务完成/提交的备注
     */
    @Column(length = 500)
    private String remark;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
