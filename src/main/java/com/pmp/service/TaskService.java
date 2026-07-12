package com.pmp.service;

import com.pmp.dto.PointsAdjustRequest;
import com.pmp.dto.PointsAdjustResponse;
import com.pmp.dto.TaskCompleteRequest;
import com.pmp.entity.TaskExecution;
import com.pmp.entity.ProjectAssignment;
import com.pmp.entity.PointsTransaction;
import com.pmp.entity.User;
import com.pmp.enumeration.TaskExecutionStatus;
import com.pmp.enumeration.TransactionType;
import com.pmp.exception.BusinessException;
import com.pmp.repository.TaskExecutionRepository;
import com.pmp.repository.AssignmentRepository;
import com.pmp.repository.PointsTransactionRepository;
import com.pmp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务执行服务类
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskExecutionRepository taskExecutionRepository;
    private final AssignmentRepository assignmentRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final UserRepository userRepository;

    /**
     * 完成任务并提交审核（赚取积分，审核通过后入账）
     */
    @Transactional
    public void completeTask(TaskCompleteRequest request, Long userId) {
        ProjectAssignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new BusinessException("ASSIGNMENT_NOT_FOUND", "分配不存在"));

        if (!assignment.getUser().getId().equals(userId)) {
            throw new BusinessException("PERMISSION_DENIED", "无权操作此任务");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "任务数量必须为正数");
        }

        // 计算积分（按单价×数量）
        BigDecimal unitPrice = assignment.getProject().getUnitPrice();
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_UNIT_PRICE", "任务单价未设置或无效");
        }
        Integer points = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity())).intValue();
        if (points <= 0) {
            throw new BusinessException("INVALID_POINTS", "积分计算异常");
        }

        // 创建任务执行记录（待审核，暂不入账）
        TaskExecution taskExecution = new TaskExecution();
        taskExecution.setAssignment(assignment);
        taskExecution.setType(TransactionType.EARN);
        taskExecution.setExecutionDate(LocalDate.now());
        taskExecution.setQuantity(request.getQuantity());
        taskExecution.setPoints(points);
        taskExecution.setRemark(request.getRemark());
        taskExecution.setStatus(TaskExecutionStatus.PENDING);
        taskExecution.setCreatedAt(LocalDateTime.now());
        taskExecutionRepository.save(taskExecution);
    }

    /**
     * 审核任务执行记录（通过则入账，拒绝则不入账）
     */
    @Transactional
    public void reviewTaskExecution(Long id, boolean approve) {
        TaskExecution taskExecution = taskExecutionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("TASK_NOT_FOUND", "任务记录不存在"));

        if (taskExecution.getStatus() != TaskExecutionStatus.PENDING) {
            throw new BusinessException("ALREADY_REVIEWED", "该任务已审核，不能重复操作");
        }

        if (approve) {
            User user = taskExecution.getAssignment().getUser();
            if (taskExecution.getPoints() == null || taskExecution.getPoints() <= 0) {
                throw new BusinessException("INVALID_POINTS", "任务积分数异常，无法入账");
            }
            PointsTransaction transaction = new PointsTransaction();
            transaction.setUser(user);
            transaction.setAssignment(taskExecution.getAssignment());
            transaction.setTaskExecution(taskExecution);
            transaction.setAmount(taskExecution.getPoints());
            transaction.setType(taskExecution.getType());
            transaction.setDescription((taskExecution.getType() == TransactionType.EARN ? "完成任务: " : "消耗积分: ")
                    + taskExecution.getAssignment().getProject().getName());
            transaction.setCreatedAt(LocalDateTime.now());
            pointsTransactionRepository.save(transaction);

            taskExecution.setStatus(TaskExecutionStatus.APPROVED);
        } else {
            taskExecution.setStatus(TaskExecutionStatus.REJECTED);
        }
        taskExecution.setReviewedAt(LocalDateTime.now());
        taskExecutionRepository.save(taskExecution);
    }

    /**
     * 获取用户积分交易记录（支持过滤）
     */
    public List<PointsTransaction> getUserTransactions(Long userId, String type,
                                                       LocalDate startDate, LocalDate endDate, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        return pointsTransactionRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .filter(t -> type == null || type.isEmpty() || t.getType().name().equals(type))
                .filter(t -> startDate == null || t.getCreatedAt() == null || !t.getCreatedAt().toLocalDate().isBefore(startDate))
                .filter(t -> endDate == null || t.getCreatedAt() == null || !t.getCreatedAt().toLocalDate().isAfter(endDate))
                .filter(t -> keyword == null || keyword.isEmpty() || t.getDescription() == null || t.getDescription().contains(keyword))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户积分余额
     */
    public Long getUserPointsBalance(Long userId) {
        return pointsTransactionRepository.sumPointsByUserId(userId);
    }

    /**
     * 获取用户的赚取类任务记录
     */
    public List<TaskExecution> getEarnTasks(Long userId) {
        return taskExecutionRepository.findByAssignment_User_IdAndTypeOrderByExecutionDateDesc(userId, TransactionType.EARN);
    }

    public Page<TaskExecution> getEarnTasks(Long userId, Pageable pageable) {
        return taskExecutionRepository.findByAssignment_User_IdAndTypeOrderByExecutionDateDesc(userId, TransactionType.EARN, pageable);
    }

    /**
     * 检查分配的任务在今天是否已完成（用于每日任务）
     */
    public boolean isCompletedToday(Long assignmentId) {
        return taskExecutionRepository.existsByAssignmentIdAndExecutionDate(assignmentId, LocalDate.now());
    }

    /**
     * 检查分配的任务是否有过完成记录
     */
    public boolean hasCompletionRecords(Long assignmentId) {
        return !taskExecutionRepository.findByAssignmentIdOrderByExecutionDateDesc(assignmentId).isEmpty();
    }

    /**
     * 获取所有积分交易记录
     */
    public List<PointsTransaction> getAllTransactions() {
        return pointsTransactionRepository.findAllByOrderByCreatedAtDesc();
    }

    public Page<PointsTransaction> getAllTransactions(Pageable pageable) {
        return pointsTransactionRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<PointsTransaction> getUserTransactionsPage(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        return pointsTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * 获取过滤后的积分交易记录（管理端使用）
     */
    public List<PointsTransaction> getFilteredTransactions(String type, LocalDate startDate, LocalDate endDate, String keyword, Long userId) {
        return pointsTransactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(t -> userId == null || t.getUser().getId().equals(userId))
                .filter(t -> type == null || type.isEmpty() || t.getType().name().equals(type))
                .filter(t -> startDate == null || t.getCreatedAt() == null || !t.getCreatedAt().toLocalDate().isBefore(startDate))
                .filter(t -> endDate == null || t.getCreatedAt() == null || !t.getCreatedAt().toLocalDate().isAfter(endDate))
                .filter(t -> keyword == null || keyword.isEmpty() || t.getDescription() == null || t.getDescription().contains(keyword))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户所有任务执行记录
     */
    public List<TaskExecution> getUserTaskExecutions(Long userId) {
        return taskExecutionRepository.findTaskExecutionsByUserId(userId);
    }

    public Page<TaskExecution> getUserTaskExecutions(Long userId, Pageable pageable) {
        return taskExecutionRepository.findTaskExecutionsByUserId(userId, pageable);
    }

    /**
     * 获取待审核的任务执行记录（管理端审核列表）
     */
    public List<TaskExecution> getPendingTasks() {
        return taskExecutionRepository.findByStatus(TaskExecutionStatus.PENDING);
    }

    public Page<TaskExecution> getPendingTasks(Pageable pageable) {
        return taskExecutionRepository.findByStatus(TaskExecutionStatus.PENDING, pageable);
    }

    /**
     * 管理员调整用户积分
     */
    @Transactional
    public PointsAdjustResponse adjustPoints(PointsAdjustRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));

        if (request.getAmount() == null || request.getAmount() == 0) {
            throw new BusinessException("INVALID_AMOUNT", "调整积分不能为0");
        }

        String typeStr = request.getAmount() >= 0 ? "EARN" : "CONSUME";

        PointsTransaction transaction = new PointsTransaction();
        transaction.setUser(user);
        transaction.setAmount(Math.abs(request.getAmount()));
        transaction.setType(TransactionType.valueOf(typeStr));
        transaction.setDescription(request.getReason() != null ? request.getReason() : "管理员手动调整");
        transaction.setCreatedAt(LocalDateTime.now());
        pointsTransactionRepository.save(transaction);

        PointsAdjustResponse response = new PointsAdjustResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setAdjustAmount(request.getAmount());
        response.setNewBalance(getUserPointsBalance(user.getId()));
        response.setMessage("积分调整成功");
        return response;
    }
}
