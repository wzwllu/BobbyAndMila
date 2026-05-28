package com.pmp.entity;

import com.pmp.enumeration.TransactionType;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 积分交易实体类
 */
@Entity
@Table(name = "points_transactions")
@Data
public class PointsTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    private Integer amount;
    
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
