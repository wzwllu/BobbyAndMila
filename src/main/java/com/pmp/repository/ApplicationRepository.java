package com.pmp.repository;

import com.pmp.entity.Application;
import com.pmp.enumeration.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务申请数据访问接口
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * 查找指定用户的全部申请，按创建时间倒序
     */
    @Query("SELECT a FROM Application a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    List<Application> findByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Application a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    Page<Application> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 按审核状态查找申请（管理端审核列表）
     */
    List<Application> findByStatus(ApplicationStatus status);

    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);

    /**
     * 判断用户是否存在待审核的申请（防止重复提交）
     */
    boolean existsByUser_IdAndStatus(Long userId, ApplicationStatus status);
}
