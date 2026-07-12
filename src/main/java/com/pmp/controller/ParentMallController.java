package com.pmp.controller;

import com.pmp.dto.redemption.BatchVerifyRequest;
import com.pmp.dto.reward.RewardRequest;
import com.pmp.dto.reward.RewardResponse;
import com.pmp.dto.redemption.RedemptionResponse;
import com.pmp.service.RewardService;
import com.pmp.service.RedemptionService;
import com.pmp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/parent/mall")
@RequiredArgsConstructor
public class ParentMallController {

    private final RewardService rewardService;
    private final RedemptionService redemptionService;
    private final UserService userService;

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        return userService.getUserByUsername(username).getId();
    }

    @GetMapping
    public String mall(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        model.addAttribute("rewards", rewardService.listAll(PageRequest.of(page, size)));
        return "parent/mall";
    }

    @GetMapping("/rewards")
    @ResponseBody
    public Page<RewardResponse> listRewards(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return rewardService.listAll(PageRequest.of(page, size));
    }

    @PostMapping("/rewards")
    @ResponseBody
    public Map<String, Object> createReward(@RequestBody RewardRequest request, Authentication authentication) {
        Long userId = getUserId(authentication);
        rewardService.createReward(request, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "奖励创建成功");
        return result;
    }

    @PutMapping("/rewards/{id}")
    @ResponseBody
    public Map<String, Object> updateReward(@PathVariable Long id, @RequestBody RewardRequest request) {
        rewardService.updateReward(id, request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "奖励更新成功");
        return result;
    }

    @PutMapping("/rewards/{id}/toggle")
    @ResponseBody
    public Map<String, Object> toggleReward(@PathVariable Long id) {
        rewardService.toggleStatus(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "状态已切换");
        return result;
    }

    @GetMapping("/redemptions")
    public String redemptions(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        model.addAttribute("pendingRedemptions", redemptionService.getPendingRedemptions());
        model.addAttribute("allRedemptions", redemptionService.getAllRedemptions(PageRequest.of(page, size)));
        return "parent/mall-redemptions";
    }

    @GetMapping("/redemptions/list")
    @ResponseBody
    public Page<RedemptionResponse> listRedemptions(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return redemptionService.getAllRedemptions(PageRequest.of(page, size));
    }

    @PostMapping("/redemptions/{id}/verify")
    @ResponseBody
    public Map<String, Object> verifyRedemption(@PathVariable Long id,
                                                @RequestBody(required = false) Map<String, String> body,
                                                Authentication authentication) {
        Long userId = getUserId(authentication);
        String remark = body != null ? body.get("reviewRemark") : null;
        redemptionService.verifyRedemption(id, userId, remark);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "核销成功，积分已扣除");
        return result;
    }

    @PostMapping("/redemptions/{id}/reject")
    @ResponseBody
    public Map<String, Object> rejectRedemption(@PathVariable Long id,
                                                @RequestBody(required = false) Map<String, String> body,
                                                Authentication authentication) {
        Long userId = getUserId(authentication);
        String remark = body != null ? body.get("reviewRemark") : null;
        redemptionService.rejectRedemption(id, userId, remark);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "兑换已拒绝");
        return result;
    }

    @PostMapping("/redemptions/batch-verify")
    @ResponseBody
    public Map<String, Object> batchVerify(@RequestBody BatchVerifyRequest request,
                                           Authentication authentication) {
        Long userId = getUserId(authentication);
        redemptionService.batchVerify(request.getIds(), userId, request.getReviewRemark());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "批量核销完成，共处理 " + request.getIds().size() + " 条记录");
        return result;
    }
}
