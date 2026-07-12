package com.pmp.enumeration;

import lombok.Getter;

@Getter
public enum RewardStatus {
    ACTIVE("上架中"),
    DISABLED("已下架");

    private final String label;

    RewardStatus(String label) {
        this.label = label;
    }
}
