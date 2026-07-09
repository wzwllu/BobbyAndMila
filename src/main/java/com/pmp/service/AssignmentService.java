package com.pmp.service;

import com.pmp.dto.AssignmentRequest;
import com.pmp.dto.AssignmentResponse;
import com.pmp.entity.ProjectAssignment;
import com.pmp.entity.Project;
import com.pmp.entity.User;
import com.pmp.enumeration.AssignmentStatus;
import com.pmp.exception.BusinessException;
import com.pmp.repository.AssignmentRepository;
import com.pmp.repository.ProjectRepository;
import com.pmp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目分配服务类
 */
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * 分配项目给用户
     */
    @Transactional
    public AssignmentResponse assignProject(AssignmentRequest request, Long createdBy) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "项目不存在"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));

        // 同一用户不能重复分配同一任务
        if (assignmentRepository.existsByUser_IdAndProject_IdAndStatus(user.getId(), project.getId(), AssignmentStatus.ACTIVE)) {
            throw new BusinessException("DUPLICATE_ASSIGNMENT", "该用户已分配此任务，无需重复分配");
        }

        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(project);
        assignment.setUser(user);
        assignment.setStartDate(request.getStartDate());
        assignment.setEndDate(request.getEndDate());
        assignment.setStatus(AssignmentStatus.ACTIVE);
        assignment.setCreatedBy(createdBy);

        ProjectAssignment saved = assignmentRepository.save(assignment);
        return convertToResponse(saved);
    }

    /**
     * 获取用户的分配列表
     */
    public List<AssignmentResponse> getUserAssignments(Long userId) {
        return assignmentRepository.findByUser_Id(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有分配
     */
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 取消分配
     */
    @Transactional
    public void cancelAssignment(Long id) {
        ProjectAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("ASSIGNMENT_NOT_FOUND", "分配不存在"));
        assignment.setStatus(AssignmentStatus.CANCELLED);
        assignmentRepository.save(assignment);
    }

    private AssignmentResponse convertToResponse(ProjectAssignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setProjectId(assignment.getProject().getId());
        response.setProjectName(assignment.getProject().getName());
        response.setProjectType(assignment.getProject().getType());
        response.setUnitPrice(assignment.getProject().getUnitPrice());
        response.setPointsToConsume(assignment.getProject().getPointsToConsume());
        response.setUserId(assignment.getUser().getId());
        response.setUserName(assignment.getUser().getUsername());
        response.setStartDate(assignment.getStartDate());
        response.setEndDate(assignment.getEndDate());
        response.setStatus(assignment.getStatus());
        response.setCreatedAt(assignment.getCreatedAt());
        return response;
    }
}
