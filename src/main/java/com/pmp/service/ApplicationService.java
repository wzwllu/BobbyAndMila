package com.pmp.service;

import com.pmp.dto.ApplyRequest;
import com.pmp.dto.AssignmentRequest;
import com.pmp.entity.Application;
import com.pmp.entity.Project;
import com.pmp.entity.User;
import com.pmp.enumeration.ApplicationStatus;
import com.pmp.enumeration.AssignmentStatus;
import com.pmp.enumeration.ProjectStatus;
import com.pmp.enumeration.ProjectType;
import com.pmp.exception.BusinessException;
import com.pmp.repository.ApplicationRepository;
import com.pmp.repository.AssignmentRepository;
import com.pmp.repository.ProjectRepository;
import com.pmp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 任务申请服务类（工人提交任务提案，审核通过后创建任务并自动分配）
 */
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentService assignmentService;

    /**
     * 工人提交新任务提案
     */
    @Transactional
    public void apply(Long userId, ApplyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));

        if (request.getType() == null) {
            throw new BusinessException("INVALID_TYPE", "任务类型不能为空");
        }
        if (request.getUnitPrice() == null || request.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_UNIT_PRICE", "单价必须大于0");
        }

        // 防止重复提交待审核的提案
        if (applicationRepository.existsByUser_IdAndStatus(userId, ApplicationStatus.PENDING)) {
            throw new BusinessException("DUPLICATE_APPLICATION", "您已有待审核的申请，请等待审核");
        }

        Application application = new Application();
        application.setUser(user);
        application.setProjectName(request.getName());
        application.setProjectType(request.getType());
        application.setUnitPrice(request.getUnitPrice());
        application.setRepeatType(request.getRepeatType());
        application.setEndDate(request.getEndDate());
        application.setRemark(request.getRemark());
        application.setStatus(ApplicationStatus.PENDING);
        applicationRepository.save(application);
    }

    /**
     * 获取待审核的申请列表
     */
    public List<Application> listPending() {
        return applicationRepository.findByStatus(ApplicationStatus.PENDING);
    }

    public Page<Application> listPending(Pageable pageable) {
        return applicationRepository.findByStatus(ApplicationStatus.PENDING, pageable);
    }

    /**
     * 获取指定用户的全部申请
     */
    public List<Application> getMyApplications(Long userId) {
        return applicationRepository.findByUserId(userId);
    }

    public Page<Application> getMyApplications(Long userId, Pageable pageable) {
        return applicationRepository.findByUserId(userId, pageable);
    }

    /**
     * 审核通过：创建任务并自动分配给申请人
     */
    @Transactional
    public void approve(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("APPLICATION_NOT_FOUND", "申请不存在"));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException("ALREADY_REVIEWED", "该申请已审核，不能重复操作");
        }

        // 创建新任务
        Project project = new Project();
        project.setName(application.getProjectName());
        project.setType(application.getProjectType());
        project.setUnitPrice(application.getUnitPrice());
        project.setRepeatType(application.getRepeatType());
        project.setEndDate(application.getEndDate());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedBy(application.getUser().getId());
        project = projectRepository.save(project);

        // 自动分配给申请人
        AssignmentRequest assignmentRequest = new AssignmentRequest();
        assignmentRequest.setProjectId(project.getId());
        assignmentRequest.setUserId(application.getUser().getId());
        assignmentRequest.setStartDate(LocalDate.now());
        assignmentRequest.setEndDate(application.getEndDate());
        assignmentService.assignProject(assignmentRequest, application.getUser().getId());

        application.setStatus(ApplicationStatus.APPROVED);
        application.setReviewedAt(java.time.LocalDateTime.now());
        applicationRepository.save(application);
    }

    /**
     * 审核拒绝
     */
    @Transactional
    public void reject(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("APPLICATION_NOT_FOUND", "申请不存在"));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException("ALREADY_REVIEWED", "该申请已审核，不能重复操作");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        application.setReviewedAt(java.time.LocalDateTime.now());
        applicationRepository.save(application);
    }
}
