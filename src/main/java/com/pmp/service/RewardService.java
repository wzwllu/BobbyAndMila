package com.pmp.service;

import com.pmp.dto.reward.RewardRequest;
import com.pmp.dto.reward.RewardResponse;
import com.pmp.entity.Reward;
import com.pmp.enumeration.RewardStatus;
import com.pmp.exception.BusinessException;
import com.pmp.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;

    @Transactional
    public RewardResponse createReward(RewardRequest request, Long parentId) {
        Reward reward = new Reward();
        reward.setName(request.getName());
        reward.setCostPoints(request.getCostPoints());
        reward.setDescription(request.getDescription());
        reward.setImageUrl(request.getImageUrl());
        reward.setStock(request.getStock());
        reward.setStatus(RewardStatus.ACTIVE);
        reward.setCreatedBy(parentId);
        Reward saved = rewardRepository.save(reward);
        return toResponse(saved);
    }

    @Transactional
    public RewardResponse updateReward(Long id, RewardRequest request) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new BusinessException("REWARD_NOT_FOUND", "奖励不存在"));
        reward.setName(request.getName());
        reward.setCostPoints(request.getCostPoints());
        reward.setDescription(request.getDescription());
        reward.setImageUrl(request.getImageUrl());
        reward.setStock(request.getStock());
        return toResponse(reward);
    }

    @Transactional
    public void toggleStatus(Long id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new BusinessException("REWARD_NOT_FOUND", "奖励不存在"));
        if (reward.getStatus() == RewardStatus.ACTIVE) {
            reward.setStatus(RewardStatus.DISABLED);
        } else {
            reward.setStatus(RewardStatus.ACTIVE);
        }
    }

    public List<RewardResponse> listActive() {
        return rewardRepository.findByStatus(RewardStatus.ACTIVE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<RewardResponse> listAll(Pageable pageable) {
        return rewardRepository.findAll(pageable).map(this::toResponse);
    }

    public RewardResponse getReward(Long id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new BusinessException("REWARD_NOT_FOUND", "奖励不存在"));
        return toResponse(reward);
    }

    private RewardResponse toResponse(Reward reward) {
        RewardResponse r = new RewardResponse();
        r.setId(reward.getId());
        r.setName(reward.getName());
        r.setCostPoints(reward.getCostPoints());
        r.setDescription(reward.getDescription());
        r.setImageUrl(reward.getImageUrl());
        r.setStock(reward.getStock());
        r.setStatus(reward.getStatus().name());
        r.setStatusLabel(reward.getStatus().getLabel());
        r.setCreatedBy(reward.getCreatedBy());
        r.setCreatedAt(reward.getCreatedAt());
        return r;
    }
}
