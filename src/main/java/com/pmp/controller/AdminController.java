package com.pmp.controller;

import com.pmp.dto.*;
import com.pmp.dto.redemption.BatchVerifyRequest;
import com.pmp.entity.TaskExecution;
import com.pmp.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * 管理端控制器
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProjectService projectService;
    private final AssignmentService assignmentService;
    private final UserService userService;
    private final TaskService taskService;
    private final ApplicationService applicationService;
    private final RedemptionService redemptionService;

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        return userService.getUserByUsername(username).getId();
    }

    /**
     * 管理端首页
     */
    @GetMapping
    public String index() {
        return "admin/index";
    }

    /**
     * 任务管理页面（仅显示启用中的任务）
     */
    @GetMapping("/projects")
    public String projects(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        model.addAttribute("projects", projectService.getActiveProjects(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "status"))));
        return "admin/projects";
    }

    /**
     * 已废弃任务页面
     */
    @GetMapping("/projects/deprecated")
    public String deprecatedProjects(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        model.addAttribute("projects", projectService.getDeprecatedProjects(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"))));
        return "admin/projects-deprecated";
    }

    /**
     * 创建任务
     */
    @PostMapping("/projects")
    @ResponseBody
    public ProjectResponse createProject(@RequestBody ProjectRequest request, Authentication authentication) {
        Long userId = getUserId(authentication);
        return projectService.createProject(request, userId);
    }

    /**
     * 废弃项目（替代删除）
     */
    @PutMapping("/projects/{id}/deprecate")
    @ResponseBody
    public void deprecateProject(@PathVariable Long id) {
        projectService.deprecateProject(id);
    }

    /**
     * 人员管理页面
     */
    @GetMapping("/users")
    public String users(Model model,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
        model.addAttribute("users", userService.getAllUsers(PageRequest.of(page, size)));
        return "admin/users";
    }

    /**
     * 创建用户
     */
    @PostMapping("/users")
    @ResponseBody
    public UserResponse createUser(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    /**
     * 管理员重置用户密码
     */
    @PostMapping("/users/{id}/password")
    @ResponseBody
    public void resetPassword(@PathVariable Long id, @RequestBody PasswordResetRequest request) {
        userService.resetPassword(id, request.getNewPassword());
    }

    /**
     * 用户统计页面
     */
    @GetMapping("/users/{id}/stats")
    public String userStats(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("stats", userService.getUserStats(id));
        return "admin/stats";
    }

    /**
     * 获取用户按天统计数据（JSON）
     */
    @GetMapping("/users/{id}/stats/daily")
    @ResponseBody
    public List<DailyStatsResponse> getDailyStats(@PathVariable Long id) {
        return userService.getDailyStats(id);
    }

    /**
     * 获取指定日期的任务执行详情（JSON）
     */
    @GetMapping("/users/{id}/stats/daily/{date}")
    @ResponseBody
    public List<Map<String, Object>> getDailyDetail(
            @PathVariable Long id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return userService.getDailyTaskDetails(id, date).stream()
                .map(te -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("projectName", te.getAssignment().getProject().getName());
                    m.put("type", te.getType().name());
                    m.put("typeLabel", te.getType().getLabel());
                    m.put("status", te.getStatus().name());
                    m.put("statusLabel", te.getStatus().getLabel());
                    m.put("quantity", te.getQuantity());
                    m.put("points", te.getPoints());
                    m.put("remark", te.getRemark());
                    return m;
                })
                .collect(Collectors.toList());
    }

    /**
     * 每日统计详情页面
     */
    @GetMapping("/users/{id}/stats/daily/{date}/view")
    public String dailyDetailView(
            @PathVariable Long id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("date", date);
        List<TaskExecution> details = userService.getDailyTaskDetails(id, date);
        model.addAttribute("details", details);
        model.addAttribute("approvedCount", details.stream().filter(t -> t.getStatus().name().equals("APPROVED")).count());
        model.addAttribute("pendingCount", details.stream().filter(t -> t.getStatus().name().equals("PENDING")).count());
        model.addAttribute("rejectedCount", details.stream().filter(t -> t.getStatus().name().equals("REJECTED")).count());
        model.addAttribute("totalPoints", details.stream().filter(t -> t.getStatus().name().equals("APPROVED")).mapToInt(t -> t.getPoints() != null ? t.getPoints() : 0).sum());
        return "admin/daily-detail";
    }

    /**
     * 获取用户所有任务执行记录（JSON，用于任务统计明细）
     */
    @GetMapping("/users/{id}/task-executions")
    @ResponseBody
    public Map<String, Object> getUserTaskExecutions(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        Page<TaskExecution> execPage = taskService.getUserTaskExecutions(id, PageRequest.of(page, size));
        List<Map<String, Object>> items = execPage.getContent().stream()
                .map(te -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("projectName", te.getAssignment().getProject().getName());
                    m.put("projectType", te.getAssignment().getProject().getType().name());
                    m.put("type", te.getType().name());
                    m.put("typeLabel", te.getType().getLabel());
                    m.put("date", te.getExecutionDate() != null ? te.getExecutionDate().toString() : "");
                    m.put("status", te.getStatus().name());
                    m.put("statusLabel", te.getStatus().getLabel());
                    m.put("quantity", te.getQuantity());
                    m.put("points", te.getPoints());
                    m.put("remark", te.getRemark());
                    return m;
                })
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("content", items);
        result.put("totalElements", execPage.getTotalElements());
        result.put("totalPages", execPage.getTotalPages());
        result.put("number", execPage.getNumber());
        result.put("size", execPage.getSize());
        return result;
    }

    /**
     * 获取用户按任务统计数据（JSON）
     */
    @GetMapping("/users/{id}/stats/tasks")
    @ResponseBody
    public List<TaskStatsResponse> getTaskStats(@PathVariable Long id) {
        return userService.getTaskStats(id);
    }

    /**
     * 获取用户列表（JSON 格式）
     */
    @GetMapping("/users/list")
    @ResponseBody
    public List<UserResponse> listUsers() {
        return userService.getAllUsers();
    }

    /**
     * 项目分配页面（仅显示未取消的分配）
     */
    @GetMapping("/assignments")
    public String assignments(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        model.addAttribute("assignments", assignmentService.getActiveAssignments(PageRequest.of(page, size)));
        model.addAttribute("projects", projectService.getActiveProjects());
        model.addAttribute("users", userService.getAllUsers());
        return "admin/assignments";
    }

    /**
     * 已取消分配页面
     */
    @GetMapping("/assignments/cancelled")
    public String cancelledAssignments(Model model,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        model.addAttribute("assignments", assignmentService.getCancelledAssignments(PageRequest.of(page, size)));
        return "admin/assignments-cancelled";
    }

    /**
     * 分配项目
     */
    @PostMapping("/assignments")
    @ResponseBody
    public AssignmentResponse assignProject(@RequestBody AssignmentRequest request, Authentication authentication) {
        Long userId = getUserId(authentication);
        return assignmentService.assignProject(request, userId);
    }

    /**
     * 取消分配
     */
    @PutMapping("/assignments/{id}/cancel")
    @ResponseBody
    public void cancelAssignment(@PathVariable Long id) {
        assignmentService.cancelAssignment(id);
    }

    @PostMapping("/redemptions/{id}/verify")
    @ResponseBody
    public Map<String, Object> verifyRedemption(@PathVariable Long id,
                                                @RequestBody(required = false) Map<String, String> body,
                                                Authentication authentication) {
        Long userId = getUserId(authentication);
        String remark = body != null ? body.get("reviewRemark") : null;
        redemptionService.verifyRedemption(id, userId, remark);
        return success("核销成功，积分已扣除");
    }

    @PostMapping("/redemptions/{id}/reject")
    @ResponseBody
    public Map<String, Object> rejectRedemption(@PathVariable Long id,
                                                @RequestBody(required = false) Map<String, String> body,
                                                Authentication authentication) {
        Long userId = getUserId(authentication);
        String remark = body != null ? body.get("reviewRemark") : null;
        redemptionService.rejectRedemption(id, userId, remark);
        return success("兑换已拒绝");
    }

    @PostMapping("/redemptions/batch-verify")
    @ResponseBody
    public Map<String, Object> batchVerify(@RequestBody BatchVerifyRequest request,
                                           Authentication authentication) {
        Long userId = getUserId(authentication);
        redemptionService.batchVerify(request.getIds(), userId, request.getReviewRemark());
        return success("批量核销完成，共处理 " + request.getIds().size() + " 条记录");
    }

    /**
     * 审核页面
     */
    @GetMapping("/reviews")
    public String reviews(Model model,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size) {
        model.addAttribute("pendingApplications", applicationService.listPending(PageRequest.of(page, size)));
        model.addAttribute("pendingTasks", taskService.getPendingTasks(PageRequest.of(page, size)));
        model.addAttribute("pendingRedemptions", redemptionService.getPendingRedemptions());
        return "admin/reviews";
    }

    /**
     * 通过任务申请 -> 自动分配
     */
    @PostMapping("/reviews/application/{id}/approve")
    @ResponseBody
    public Map<String, Object> approveApplication(@PathVariable Long id) {
        applicationService.approve(id);
        return success("申请已通过，已自动分配任务");
    }

    /**
     * 拒绝任务申请
     */
    @PostMapping("/reviews/application/{id}/reject")
    @ResponseBody
    public Map<String, Object> rejectApplication(@PathVariable Long id) {
        applicationService.reject(id);
        return success("申请已拒绝");
    }

    /**
     * 通过任务执行（入账）
     */
    @PostMapping("/reviews/task/{id}/approve")
    @ResponseBody
    public Map<String, Object> approveTask(@PathVariable Long id) {
        taskService.reviewTaskExecution(id, true);
        return success("审核通过，积分已入账");
    }

    /**
     * 拒绝任务执行
     */
    @PostMapping("/reviews/task/{id}/reject")
    @ResponseBody
    public Map<String, Object> rejectTask(@PathVariable Long id) {
        taskService.reviewTaskExecution(id, false);
        return success("任务已拒绝");
    }

    /**
     * 积分管理页面
     */
    @GetMapping("/points")
    public String points(Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size) {
        model.addAttribute("transactions", taskService.getAllTransactions(PageRequest.of(page, size)));
        return "admin/points";
    }

    /**
     * 获取过滤后的积分交易记录（JSON）
     */
    @GetMapping("/points/transactions")
    @ResponseBody
    public Map<String, Object> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Map<String, Object>> items = taskService.getFilteredTransactions(type, startDate, endDate, keyword, userId).stream()
                .map(tx -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", tx.getId());
                    m.put("username", tx.getUser().getUsername());
                    m.put("type", tx.getType().name());
                    m.put("typeLabel", tx.getType().getLabel());
                    m.put("amount", tx.getAmount());
                    m.put("description", tx.getDescription());
                    m.put("createdAt", tx.getCreatedAt() != null ?
                            tx.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
                    return m;
                })
                .collect(Collectors.toList());
        int total = items.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min((page + 1) * size, total);
        List<Map<String, Object>> pageItems = items.subList(fromIndex, toIndex);
        Map<String, Object> result = new HashMap<>();
        result.put("content", pageItems);
        result.put("totalElements", total);
        result.put("totalPages", (total + size - 1) / size);
        result.put("number", page);
        result.put("size", size);
        return result;
    }

    /**
     * 获取指定用户的积分交易记录（JSON，用于统计页）
     */
    @GetMapping("/users/{id}/transactions")
    @ResponseBody
    public Map<String, Object> getUserTransactions(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        List<Map<String, Object>> items = taskService.getFilteredTransactions(null, null, null, null, id).stream()
                .map(tx -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("type", tx.getType().name());
                    m.put("typeLabel", tx.getType().getLabel());
                    m.put("amount", tx.getAmount());
                    m.put("description", tx.getDescription());
                    m.put("createdAt", tx.getCreatedAt() != null ?
                            tx.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
                    return m;
                })
                .collect(Collectors.toList());
        int total = items.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min((page + 1) * size, total);
        List<Map<String, Object>> pageItems = items.subList(fromIndex, toIndex);
        Map<String, Object> result = new HashMap<>();
        result.put("content", pageItems);
        result.put("totalElements", total);
        result.put("totalPages", (total + size - 1) / size);
        result.put("number", page);
        result.put("size", size);
        return result;
    }

    /**
     * 管理员调整积分
     */
    @PostMapping("/points/adjust")
    @ResponseBody
    public PointsAdjustResponse adjustPoints(@RequestBody PointsAdjustRequest request) {
        return taskService.adjustPoints(request);
    }

    private Map<String, Object> success(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }
}
