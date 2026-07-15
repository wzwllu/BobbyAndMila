package com.pmp.repository;

import com.pmp.entity.Project;
import com.pmp.entity.ProjectAssignment;
import com.pmp.entity.User;
import com.pmp.enumeration.AssignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    List<ProjectAssignment> findByUserAndStartDate(User user, LocalDate startDate);

    /**
     * 查找工作端用户在指定日期的特定项目分配
     */
    Optional<ProjectAssignment> findByUserAndProjectAndStartDate(User user, Project project, LocalDate startDate);

    /**
     * 查找指定日期的活跃分配
     */
    @Query("SELECT pa FROM ProjectAssignment pa WHERE pa.startDate = :date AND pa.status = 'ACTIVE'")
    List<ProjectAssignment> findActiveAssignmentsByDate(@Param("date") LocalDate date);

    /**
     * 查找工作端用户的所有分配
     */
    List<ProjectAssignment> findByUser(User user);

    /**
     * 根据用户ID查找分配
     */
    List<ProjectAssignment> findByUser_Id(Long userId);

    /**
     * 根据用户ID和项目ID查找分配
     */
    Optional<ProjectAssignment> findByUser_IdAndProject_Id(Long userId, Long projectId);

    /**
     * 根据项目ID删除该项目下的所有分配
     */
    void deleteByProject_Id(Long projectId);

    /**
     * 判断用户是否存在指定状态的分配
     */
    boolean existsByUser_IdAndStatus(Long userId, com.pmp.enumeration.AssignmentStatus status);

    /**
     * 判断用户是否存在指定项目的活跃分配
     */
    boolean existsByUser_IdAndProject_IdAndStatus(Long userId, Long projectId, com.pmp.enumeration.AssignmentStatus status);

    /**
     * 按状态分页查询
     */
    Page<ProjectAssignment> findByStatus(AssignmentStatus status, Pageable pageable);

    /**
     * 按状态排除分页查询（未取消的分配列表用）
     */
    Page<ProjectAssignment> findByStatusNot(AssignmentStatus status, Pageable pageable);
}
