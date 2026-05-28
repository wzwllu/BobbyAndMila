package com.pmp.repository;

import com.pmp.entity.TaskExecution;
import com.pmp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 任务执行数据访问接口
 */
@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {

    /**
     * 查找指定日期的任务执行记录，按创建时间倒序
     */
    List<TaskExecution> findByExecutionDateOrderByCreatedAtDesc(LocalDate date);

    /**
     * 查找工作端用户的任务执行记录，按执行日期倒序
     */
    List<TaskExecution> findByAssignmentWorkerOrderByExecutionDateDesc(User worker);

    /**
     * 计算工作端用户获得的总积分
     */
    @Query("SELECT COALESCE(SUM(te.pointsEarned), 0) FROM TaskExecution te WHERE te.assignment.worker = :worker")
    Integer sumPointsEarnedByWorker(@Param("worker") User worker);

    /**
     * 计算工作端用户消耗的总积分
     */
    @Query("SELECT COALESCE(SUM(te.pointsConsumed), 0) FROM TaskExecution te WHERE te.assignment.worker = :worker")
    Integer sumPointsConsumedByWorker(@Param("worker") User worker);
}
