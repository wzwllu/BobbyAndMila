package com.pmp.service;

import com.pmp.dto.ProjectRequest;
import com.pmp.dto.ProjectResponse;
import com.pmp.entity.Project;
import com.pmp.enumeration.ProjectStatus;
import com.pmp.enumeration.ProjectType;
import com.pmp.exception.BusinessException;
import com.pmp.repository.AssignmentRepository;
import com.pmp.repository.ProjectRepository;
import com.pmp.repository.TaskExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目服务类
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AssignmentRepository assignmentRepository;
    private final TaskExecutionRepository taskExecutionRepository;

    /**
     * 创建项目
     */
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, Long createdBy) {
        if (request.getUnitPrice() == null || request.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_UNIT_PRICE", "单价必须大于0");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setType(ProjectType.EARN);
        project.setUnitPrice(request.getUnitPrice());
        project.setRepeatType(request.getRepeatType());
        project.setEndDate(request.getEndDate());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedBy(createdBy);

        Project saved = projectRepository.save(project);
        return convertToResponse(saved);
    }

    /**
     * 获取所有项目（管理端，含废弃状态）
     */
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(this::convertToResponse);
    }

    /**
     * 获取启用中的项目（分页，管理端任务列表用）
     */
    public Page<ProjectResponse> getActiveProjects(Pageable pageable) {
        return projectRepository.findByStatusOrderByStatusAsc(ProjectStatus.ACTIVE, pageable).map(this::convertToResponse);
    }

    /**
     * 获取启用中的项目（工人申请页、分配下拉用）
     */
    public List<ProjectResponse> getActiveProjects() {
        return projectRepository.findByStatus(ProjectStatus.ACTIVE).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取已废弃的项目（分页）
     */
    public Page<ProjectResponse> getDeprecatedProjects(Pageable pageable) {
        return projectRepository.findByStatusOrderByStatusAsc(ProjectStatus.DEPRECATED, pageable).map(this::convertToResponse);
    }

    /**
     * 获取项目详情
     */
    public ProjectResponse getProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "项目不存在"));
        return convertToResponse(project);
    }

    /**
     * 废弃项目（不再承接新任务，但保留历史数据与已存在的分配）
     */
    @Transactional
    public void deprecateProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "项目不存在"));
        project.setStatus(ProjectStatus.DEPRECATED);
        projectRepository.save(project);
    }

    private ProjectResponse convertToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setType(project.getType());
        response.setStatus(project.getStatus());
        response.setUnitPrice(project.getUnitPrice());
        response.setRepeatType(project.getRepeatType());
        response.setEndDate(project.getEndDate());
        response.setCreatedBy(project.getCreatedBy());
        response.setCreatedAt(project.getCreatedAt());
        return response;
    }
}
