package com.pmp.enumeration;

import lombok.Getter;

@Getter
public enum RedemptionStatus {
    UNVERIFIED("未核销"),
    PENDING("待核销"),
    APPROVED("已核销"),
    REJECTED("已拒绝");

    private final String label;

    RedemptionStatus(String label) {
        this.label = label;
    }
}
