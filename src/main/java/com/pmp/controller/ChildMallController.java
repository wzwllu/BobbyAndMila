package com.pmp.controller;

import com.pmp.dto.redemption.RedemptionRequest;
import com.pmp.dto.redemption.RedemptionResponse;
import com.pmp.dto.reward.RewardResponse;
import com.pmp.service.RedemptionService;
import com.pmp.service.RewardService;
import com.pmp.service.TaskService;
import com.pmp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/child/mall")
@RequiredArgsConstructor
public class ChildMallController {

    private final RewardService rewardService;
    private final RedemptionService redemptionService;
    private final TaskService taskService;
    private final UserService userService;

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        return userService.getUserByUsername(username).getId();
    }

    @GetMapping
    public String mall(Authentication authentication, Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(authentication);
        model.addAttribute("rewards", rewardService.listActive());
        model.addAttribute("balance", taskService.getUserPointsBalance(userId));
        model.addAttribute("myRedemptions", redemptionService.getMyRedemptions(userId, PageRequest.of(page, size)));
        return "child/mall";
    }

    @GetMapping("/rewards")
    @ResponseBody
    public List<RewardResponse> listRewards() {
        return rewardService.listActive();
    }

    @GetMapping("/balance")
    @ResponseBody
    public Map<String, Long> getBalance(Authentication authentication) {
        Long userId = getUserId(authentication);
        Map<String, Long> result = new HashMap<>();
        result.put("balance", taskService.getUserPointsBalance(userId));
        return result;
    }

    @PostMapping("/redeem")
    @ResponseBody
    public Map<String, Object> redeem(@RequestBody RedemptionRequest request, Authentication authentication) {
        Long userId = getUserId(authentication);
        redemptionService.submitRedemption(userId, request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "兑换成功，积分已扣除，奖励已存入宝箱");
        result.put("balance", taskService.getUserPointsBalance(userId));
        return result;
    }

    @GetMapping("/redemptions")
    @ResponseBody
    public Page<RedemptionResponse> myRedemptions(Authentication authentication,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(authentication);
        return redemptionService.getMyRedemptions(userId, PageRequest.of(page, size));
    }

    @GetMapping("/treasure")
    public String treasureBox(Authentication authentication, Model model) {
        Long userId = getUserId(authentication);
        model.addAttribute("items", redemptionService.getTreasureBoxItems(userId));
        model.addAttribute("balance", taskService.getUserPointsBalance(userId));
        return "child/treasure";
    }

    @GetMapping("/treasure/items")
    @ResponseBody
    public List<RedemptionResponse> treasureItems(Authentication authentication) {
        Long userId = getUserId(authentication);
        return redemptionService.getTreasureBoxItems(userId);
    }

    @PostMapping("/treasure/{id}/request-verification")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> requestVerification(@PathVariable Long id,
                                                                    @RequestBody Map<String, String> body,
                                                                    Authentication authentication) {
        Long userId = getUserId(authentication);
        String usedAtStr = body.get("usedAt");
        if (usedAtStr == null || usedAtStr.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "请选择使用时间");
            return ResponseEntity.badRequest().body(err);
        }
        LocalDateTime usedAt = LocalDateTime.parse(usedAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        redemptionService.requestVerification(id, userId, usedAt);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "核销申请已提交，等待家长审核");
        return ResponseEntity.ok(result);
    }
}
