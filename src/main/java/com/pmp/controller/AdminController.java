package com.pmp.controller;

import com.pmp.dto.*;
import com.pmp.enumeration.Role;
import com.pmp.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 任务管理页面
     */
    @GetMapping("/projects")
    public String projects(Model model) {
        model.addAttribute("projects", projectService.getAllProjects());
        return "admin/projects";
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
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
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
     * 获取用户统计数据
     */
    @GetMapping("/users/{id}/stats")
    @ResponseBody
    public UserStatsResponse getUserStats(@PathVariable Long id) {
        return userService.getUserStats(id);
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
     * 项目分配页面
     */
    @GetMapping("/assignments")
    public String assignments(Model model) {
        model.addAttribute("assignments", assignmentService.getAllAssignments());
        model.addAttribute("projects", projectService.getAllProjects());
        model.addAttribute("users", userService.getAllUsers());
        return "admin/assignments";
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

    /**
     * 审核页面
     */
    @GetMapping("/reviews")
    public String reviews(Model model) {
        model.addAttribute("pendingApplications", applicationService.listPending());
        model.addAttribute("pendingTasks", taskService.getPendingTasks());
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
    public String points(Model model) {
        model.addAttribute("transactions", taskService.getAllTransactions());
        return "admin/points";
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
