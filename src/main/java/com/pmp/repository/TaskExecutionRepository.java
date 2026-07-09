package com.pmp.repository;

import com.pmp.entity.TaskExecution;
import com.pmp.enumeration.TaskExecutionStatus;
import com.pmp.enumeration.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务执行数据访问接口
 */
@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {

    /**
     * 根据用户ID查找任务执行记录，按执行日期倒序
     */
    @Query("SELECT te FROM TaskExecution te WHERE te.assignment.user.id = :userId ORDER BY te.executionDate DESC")
    List<TaskExecution> findTaskExecutionsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和任务方向查找任务执行记录（工人端按赚取/消耗分页展示）
     */
    List<TaskExecution> findByAssignment_User_IdAndTypeOrderByExecutionDateDesc(Long userId, TransactionType type);

    /**
     * 按审核状态查找任务执行记录（管理端审核列表）
     */
    List<TaskExecution> findByStatus(TaskExecutionStatus status);

    /**
     * 统计用户已完成的任务数（审核通过）
     */
    @Query("SELECT COUNT(te) FROM TaskExecution te WHERE te.assignment.user.id = :userId AND te.status = 'APPROVED'")
    Long countCompletedByUserId(@Param("userId") Long userId);

    /**
     * 统计用户未完成的任务数（待审核 + 已拒绝）
     */
    @Query("SELECT COUNT(te) FROM TaskExecution te WHERE te.assignment.user.id = :userId AND te.status IN ('PENDING', 'REJECTED')")
    Long countIncompleteByUserId(@Param("userId") Long userId);

    /**
     * 根据项目ID删除该项目下的所有任务执行记录
     */
    @Modifying
    @Query("DELETE FROM TaskExecution te WHERE te.assignment.project.id = :projectId")
    void deleteByAssignmentProjectId(@Param("projectId") Long projectId);
}
