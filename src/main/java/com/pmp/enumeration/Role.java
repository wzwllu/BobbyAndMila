package com.pmp.enumeration;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum Role {
    /**
     * 管理员
     */
    ADMIN("管理员"),

    /**
     * 工人
     */
    WORKER("工人");

    private final String label;

    Role(String label) {
        this.label = label;
    }
}
