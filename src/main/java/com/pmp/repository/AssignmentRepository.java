package com.pmp.repository;

import com.pmp.entity.Project;
import com.pmp.entity.ProjectAssignment;
import com.pmp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 项目分配数据访问接口
 */
@Repository
public interface AssignmentRepository extends JpaRepository<ProjectAssignment, Long> {

    /**
     * 查找工作端用户在指定日期的所有分配
     */
    List<ProjectAssignment> findByWorkerAndAssignDate(User worker, LocalDate assignDate);

    /**
     * 查找工作端用户在指定日期的特定项目分配
     */
    Optional<ProjectAssignment> findByWorkerAndProjectAndAssignDate(User worker, Project project, LocalDate assignDate);

    /**
     * 查找指定日期的活跃分配
     */
    @Query("SELECT pa FROM ProjectAssignment pa WHERE pa.assignDate = :date AND pa.status = 'ACTIVE'")
    List<ProjectAssignment> findActiveAssignmentsByDate(@Param("date") LocalDate date);

    /**
     * 查找工作端用户的所有分配
     */
    List<ProjectAssignment> findByWorker(User worker);
}
