package com.pmp.entity;

import com.pmp.enumeration.AssignmentStatus;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * 项目分配实体类
 */
@Entity
@Table(name = "project_assignments")
@Data
public class ProjectAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;
    
    @Column(name = "assign_date", nullable = false)
    private LocalDate assignDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;
    
    @Column(name = "completed_quantity")
    private Integer completedQuantity;
}
