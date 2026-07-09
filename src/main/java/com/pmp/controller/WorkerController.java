package com.pmp.controller;

import com.pmp.dto.*;
import com.pmp.enumeration.AssignmentStatus;
import com.pmp.enumeration.ProjectType;
import com.pmp.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作端控制器
 */
@Controller
@RequestMapping("/worker")
@RequiredArgsConstructor
public class WorkerController {

    private final AssignmentService assignmentService;
    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserService userService;
    private final ApplicationService applicationService;

    /**
     * 工作端首页
     */
    @GetMapping
    public String index() {
        return "worker/index";
    }

    /**
     * 从 Authentication 获取当前用户 ID
     */
    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        return userService.getUserByUsername(username).getId();
    }

    /**
     * 赚取积分任务页面（仅显示增加积分类、进行中的分配）
     */
    @GetMapping("/tasks")
    public String tasks(Authentication authentication, Model model) {
        Long userId = getUserId(authentication);
        List<AssignmentResponse> earnAssignments = assignmentService.getUserAssignments(userId).stream()
                .filter(a -> a.getStatus() == AssignmentStatus.ACTIVE && a.getProjectType() == ProjectType.EARN)
                .collect(Collectors.toList());
        model.addAttribute("assignments", earnAssignments);
        model.addAttribute("earnTasks", taskService.getEarnTasks(userId));
        return "worker/tasks";
    }

    /**
     * 完成任务（提交审核）
     */
    @PostMapping("/tasks/complete")
    @ResponseBody
    public Map<String, Object> completeTask(@RequestBody TaskCompleteRequest request, Authentication authentication) {
        Long userId = getUserId(authentication);
        taskService.completeTask(request, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "任务已提交，等待审核");
        response.put("balance", taskService.getUserPointsBalance(userId));
        return response;
    }

    /**
     * 消耗积分任务页面（仅显示消耗积分类、进行中的分配）
     */
    @GetMapping("/consume")
    public String consume(Authentication authentication, Model model) {
        Long userId = getUserId(authentication);
        List<AssignmentResponse> consumeAssignments = assignmentService.getUserAssignments(userId).stream()
                .filter(a -> a.getStatus() == AssignmentStatus.ACTIVE && a.getProjectType() == ProjectType.CONSUME)
                .collect(Collectors.toList());
        model.addAttribute("assignments", consumeAssignments);
        model.addAttribute("consumeTasks", taskService.getConsumeTasks(userId));
        return "worker/consume";
    }

    /**
     * 提交消耗积分（审核）
     */
    @PostMapping("/consume/submit")
    @ResponseBody
    public Map<String, Object> submitConsume(@RequestBody PointsConsumeRequest request, Authentication authentication) {
        Long userId = getUserId(authentication);
        taskService.submitConsume(request, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "消耗申请已提交，等待审核");
        response.put("balance", taskService.getUserPointsBalance(userId));
        return response;
    }

    /**
     * 申请任务页面（工人提交新任务提案）
     */
    @GetMapping("/apply")
    public String apply(Authentication authentication, Model model) {
        Long userId = getUserId(authentication);
        model.addAttribute("myApplications", applicationService.getMyApplications(userId));
        return "worker/apply";
    }

    /**
     * 提交新任务提案
     */
    @PostMapping("/apply/submit")
    @ResponseBody
    public Map<String, Object> submitApply(@RequestBody ApplyRequest request, Authentication authentication) {
        Long userId = getUserId(authentication);
        applicationService.apply(userId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "申请已提交，等待审核");
        return response;
    }

    /**
     * 积分明细页面
     */
    @GetMapping("/points")
    public String points(Authentication authentication, Model model) {
        Long userId = getUserId(authentication);
        Long balance = taskService.getUserPointsBalance(userId);
        model.addAttribute("balance", balance);
        model.addAttribute("transactions", taskService.getAllTransactions().stream()
                .filter(t -> t.getUser().getId().equals(userId))
                .collect(Collectors.toList()));
        return "worker/points";
    }

    /**
     * 获取当前用户积分余额
     */
    @GetMapping("/points/balance")
    @ResponseBody
    public Map<String, Long> getBalance(Authentication authentication) {
        Long userId = getUserId(authentication);
        Map<String, Long> response = new HashMap<>();
        response.put("balance", taskService.getUserPointsBalance(userId));
        return response;
    }
}
