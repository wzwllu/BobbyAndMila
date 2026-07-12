package com.pmp.repository;

import com.pmp.entity.Reward;
import com.pmp.enumeration.RewardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
    List<Reward> findByStatus(RewardStatus status);

    Page<Reward> findAll(Pageable pageable);

    List<Reward> findByCreatedBy(Long createdBy);
}
