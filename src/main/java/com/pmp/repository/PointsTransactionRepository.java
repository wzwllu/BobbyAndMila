package com.pmp.repository;

import com.pmp.entity.PointsTransaction;
import com.pmp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 积分交易数据访问接口
 */
@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {

    /**
     * 查找用户的积分交易记录，按创建时间倒序
     */
    List<PointsTransaction> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 计算用户获得的总积分
     */
    @Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PointsTransaction pt WHERE pt.user = :user AND pt.type = 'EARN'")
    Long sumEarnedByUser(@Param("user") User user);

    /**
     * 计算用户消耗的总积分
     */
    @Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PointsTransaction pt WHERE pt.user = :user AND pt.type = 'CONSUME'")
    Long sumConsumedByUser(@Param("user") User user);
}
