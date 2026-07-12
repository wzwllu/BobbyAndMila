package com.pmp.service;

import com.pmp.dto.redemption.RedemptionRequest;
import com.pmp.dto.redemption.RedemptionResponse;
import com.pmp.entity.PointsTransaction;
import com.pmp.entity.Redemption;
import com.pmp.entity.Reward;
import com.pmp.entity.User;
import com.pmp.enumeration.RedemptionStatus;
import com.pmp.enumeration.TransactionType;
import com.pmp.exception.BusinessException;
import com.pmp.repository.PointsTransactionRepository;
import com.pmp.repository.RedemptionRepository;
import com.pmp.repository.RewardRepository;
import com.pmp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedemptionService {

    private final RedemptionRepository redemptionRepository;
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;
    private final PointsTransactionRepository pointsTransactionRepository;

    @Transactional
    public void submitRedemption(Long userId, RedemptionRequest request) {
        Reward reward = rewardRepository.findById(request.getRewardId())
                .orElseThrow(() -> new BusinessException("REWARD_NOT_FOUND", "奖励不存在"));

        if (reward.getStatus() != com.pmp.enumeration.RewardStatus.ACTIVE) {
            throw new BusinessException("REWARD_NOT_ACTIVE", "该奖励已下架");
        }

        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new BusinessException("INVALID_QUANTITY", "兑换数量非法");
        }

        if (reward.getStock() != null && request.getQuantity() > reward.getStock()) {
            throw new BusinessException("INSUFFICIENT_STOCK", "库存不足，剩余 " + reward.getStock());
        }

        Integer totalPoints = reward.getCostPoints() * request.getQuantity();
        Long balance = getUserPointsBalance(userId);
        if (balance < totalPoints) {
            throw new BusinessException("INSUFFICIENT_BALANCE",
                    "积分不足，当前 " + balance + "，需要 " + totalPoints);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));

        PointsTransaction tx = new PointsTransaction();
        tx.setUser(user);
        tx.setAmount(totalPoints);
        tx.setType(TransactionType.CONSUME);
        tx.setDescription("兑换: " + reward.getName() + " x" + request.getQuantity());
        tx.setCreatedAt(LocalDateTime.now());
        pointsTransactionRepository.save(tx);

        Redemption redemption = new Redemption();
        redemption.setUser(user);
        redemption.setReward(reward);
        redemption.setQuantity(request.getQuantity());
        redemption.setTotalPoints(totalPoints);
        redemption.setRemark(request.getRemark());
        redemption.setStatus(RedemptionStatus.UNVERIFIED);
        redemptionRepository.save(redemption);
    }

    @Transactional
    public void requestVerification(Long redemptionId, Long userId, LocalDateTime usedAt) {
        Redemption redemption = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new BusinessException("REDEMPTION_NOT_FOUND", "兑换记录不存在"));

        if (!redemption.getUser().getId().equals(userId)) {
            throw new BusinessException("NOT_YOUR_REDEMPTION", "无权操作该兑换");
        }

        if (redemption.getStatus() != RedemptionStatus.UNVERIFIED) {
            throw new BusinessException("INVALID_STATUS", "该奖励已提交核销，请等待家长审核");
        }

        redemption.setStatus(RedemptionStatus.PENDING);
        redemption.setUsedAt(usedAt);
    }

    @Transactional
    public void verifyRedemption(Long redemptionId, Long parentId, String reviewRemark) {
        Redemption redemption = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new BusinessException("REDEMPTION_NOT_FOUND", "兑换记录不存在"));

        if (redemption.getStatus() != RedemptionStatus.PENDING) {
            throw new BusinessException("ALREADY_REVIEWED", "该兑换已核销，不能重复操作");
        }

        doVerify(redemption, parentId, reviewRemark);
    }

    @Transactional
    public void batchVerify(List<Long> ids, Long parentId, String reviewRemark) {
        List<Redemption> redemptions = redemptionRepository.findAllById(ids);
        for (Redemption r : redemptions) {
            if (r.getStatus() == RedemptionStatus.PENDING) {
                doVerify(r, parentId, reviewRemark);
            }
        }
    }

    private void doVerify(Redemption redemption, Long parentId, String reviewRemark) {
        Reward reward = redemption.getReward();
        if (reward.getStock() != null) {
            if (reward.getStock() < redemption.getQuantity()) {
                redemption.setStatus(RedemptionStatus.REJECTED);
                redemption.setVerifiedBy(parentId);
                redemption.setVerifiedAt(LocalDateTime.now());
                redemption.setReviewRemark("库存不足，自动拒绝");
                refundPoints(redemption);
                return;
            }
            reward.setStock(reward.getStock() - redemption.getQuantity());
        }

        redemption.setStatus(RedemptionStatus.APPROVED);
        redemption.setVerifiedBy(parentId);
        redemption.setVerifiedAt(LocalDateTime.now());
        redemption.setReviewRemark(reviewRemark);
    }

    @Transactional
    public void rejectRedemption(Long redemptionId, Long parentId, String reviewRemark) {
        Redemption redemption = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new BusinessException("REDEMPTION_NOT_FOUND", "兑换记录不存在"));

        if (redemption.getStatus() != RedemptionStatus.PENDING) {
            throw new BusinessException("ALREADY_REVIEWED", "该兑换已核销，不能重复操作");
        }

        redemption.setStatus(RedemptionStatus.REJECTED);
        redemption.setVerifiedBy(parentId);
        redemption.setVerifiedAt(LocalDateTime.now());
        redemption.setReviewRemark(reviewRemark);
        refundPoints(redemption);
    }

    private void refundPoints(Redemption redemption) {
        PointsTransaction tx = new PointsTransaction();
        tx.setUser(redemption.getUser());
        tx.setAmount(redemption.getTotalPoints());
        tx.setType(TransactionType.EARN);
        tx.setDescription("退回: " + redemption.getReward().getName() + " x" + redemption.getQuantity());
        tx.setCreatedAt(LocalDateTime.now());
        pointsTransactionRepository.save(tx);
    }

    public List<RedemptionResponse> getTreasureBoxItems(Long userId) {
        return redemptionRepository.findByUserIdAndStatus(userId, RedemptionStatus.UNVERIFIED).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<RedemptionResponse> getMyRedemptions(Long userId, Pageable pageable) {
        return redemptionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public Page<RedemptionResponse> getAllRedemptions(Pageable pageable) {
        return redemptionRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    public Page<RedemptionResponse> getFilteredRedemptions(String userName, String rewardName,
                                                            String statusStr, LocalDateTime startDate,
                                                            LocalDateTime endDate, Pageable pageable) {
        RedemptionStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            status = RedemptionStatus.valueOf(statusStr);
        }
        return redemptionRepository.findByFilters(
                userName, rewardName, status, startDate, endDate, pageable)
                .map(this::toResponse);
    }

    public List<RedemptionResponse> getPendingRedemptions() {
        return redemptionRepository.findByStatus(RedemptionStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<RedemptionResponse> getPendingRedemptions(Pageable pageable) {
        return redemptionRepository.findByStatus(RedemptionStatus.PENDING, pageable)
                .map(this::toResponse);
    }

    private Long getUserPointsBalance(Long userId) {
        return pointsTransactionRepository.sumPointsByUserId(userId);
    }

    private RedemptionResponse toResponse(Redemption redemption) {
        RedemptionResponse r = new RedemptionResponse();
        r.setId(redemption.getId());
        r.setUserId(redemption.getUser().getId());
        r.setUserName(redemption.getUser().getUsername());
        r.setRewardId(redemption.getReward().getId());
        r.setRewardName(redemption.getReward().getName());
        r.setCostPoints(redemption.getReward().getCostPoints());
        r.setQuantity(redemption.getQuantity());
        r.setTotalPoints(redemption.getTotalPoints());
        r.setRemark(redemption.getRemark());
        r.setStatus(redemption.getStatus().name());
        r.setStatusLabel(redemption.getStatus().getLabel());
        r.setUsedAt(redemption.getUsedAt());
        r.setReviewRemark(redemption.getReviewRemark());
        r.setVerifiedAt(redemption.getVerifiedAt());
        r.setCreatedAt(redemption.getCreatedAt());
        return r;
    }
}
