package com.pmp.repository;

import com.pmp.entity.Redemption;
import com.pmp.enumeration.RedemptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    Page<Redemption> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Redemption> findByUserIdAndStatus(Long userId, RedemptionStatus status);

    Page<Redemption> findByUserIdAndStatus(Long userId, RedemptionStatus status, Pageable pageable);

    List<Redemption> findByStatus(RedemptionStatus status);

    Page<Redemption> findByStatus(RedemptionStatus status, Pageable pageable);

    Page<Redemption> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByUserIdAndStatus(Long userId, RedemptionStatus status);

    @Query("SELECT r FROM Redemption r WHERE " +
           "(:userName IS NULL OR r.user.username LIKE %:userName%) AND " +
           "(:rewardName IS NULL OR r.reward.name LIKE %:rewardName%) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:startDate IS NULL OR r.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR r.createdAt <= :endDate) " +
           "ORDER BY r.createdAt DESC")
    Page<Redemption> findByFilters(@Param("userName") String userName,
                                   @Param("rewardName") String rewardName,
                                   @Param("status") RedemptionStatus status,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);
}
