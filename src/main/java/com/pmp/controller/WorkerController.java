package com.pmp.controller;

import com.pmp.dto.*;
import com.pmp.enumeration.AssignmentStatus;
import com.pmp.enumeration.ProjectType;
import com.pmp.enumeration.RepeatType;
import com.pmp.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    public String tasks(Authentication authentication, Model model,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(authentication);
        List<AssignmentResponse> earnAssignments = assignmentService.getUserAssignments(userId).stream()
                .filter(a -> a.getStatus() == AssignmentStatus.ACTIVE && a.getProjectType() == ProjectType.EARN)
                .collect(Collectors.toList());
        model.addAttribute("assignments", earnAssignments);

        Map<Long, String> completionStatus = new HashMap<>();
        for (AssignmentResponse a : earnAssignments) {
            if (a.getRepeatType() == RepeatType.DAILY) {
                completionStatus.put(a.getId(), taskService.isCompletedToday(a.getId()) ? "completed" : "pending");
            } else {
                boolean hasDone = taskService.hasCompletionRecords(a.getId());
                if (a.getEndDate() != null && a.getEndDate().isBefore(java.time.LocalDate.now()) && !hasDone) {
                    completionStatus.put(a.getId(), "expired");
                } else if (hasDone) {
                    completionStatus.put(a.getId(), "completed");
                } else {
                    completionStatus.put(a.getId(), "pending");
                }
            }
        }
        model.addAttribute("completionStatus", completionStatus);
        model.addAttribute("earnTasks", taskService.getEarnTasks(userId, PageRequest.of(page, size)));
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
     * 申请任务页面（工人提交新任务提案）
     */
    @GetMapping("/apply")
    public String apply(Authentication authentication, Model model,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(authentication);
        model.addAttribute("myApplications", applicationService.getMyApplications(userId, PageRequest.of(page, size)));
        return "worker/apply";
    }

    /**
     * 创建新任务提案页面
     */
    @GetMapping("/apply/new")
    public String applyNew() {
        return "worker/apply-new";
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
    public String points(Authentication authentication, Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(authentication);
        Long balance = taskService.getUserPointsBalance(userId);
        model.addAttribute("balance", balance);
        model.addAttribute("transactions", taskService.getUserTransactionsPage(userId, PageRequest.of(page, size)));
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

    /**
     * 获取当前用户积分交易记录（支持过滤）
     */
    @GetMapping("/points/transactions")
    @ResponseBody
    public Map<String, Object> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        List<Map<String, Object>> items = taskService.getUserTransactions(userId, type, startDate, endDate, keyword).stream()
                .map(tx -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("type", tx.getType().name());
                    m.put("typeLabel", tx.getType().getLabel());
                    m.put("description", tx.getDescription());
                    m.put("amount", tx.getAmount());
                    m.put("createdAt", tx.getCreatedAt() != null ?
                            tx.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
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
}
