package com.pmp.entity;

import com.pmp.enumeration.ProjectType;
import com.pmp.enumeration.ProjectStatus;
import com.pmp.enumeration.RepeatType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目实体类
 */
@Entity
@Table(name = "projects")
@Data
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @Column(scale = 2)
    private BigDecimal unitPrice;
    
    @Enumerated(EnumType.STRING)
    private RepeatType repeatType;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "points_to_consume")
    private Integer pointsToConsume;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
