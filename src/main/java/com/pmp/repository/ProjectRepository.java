package com.pmp.repository;

import com.pmp.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 项目数据访问接口
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 根据创建者ID查找项目列表
     */
    List<Project> findByCreatedBy(Long createdBy);
}
