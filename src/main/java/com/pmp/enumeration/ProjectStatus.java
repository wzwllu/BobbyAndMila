package com.pmp.enumeration;
import lombok.Getter;

/**
 * 项目状态枚举
 */
@Getter
public enum ProjectStatus {
    /**
     * 启用中
     */
    ACTIVE("启用"),

    /**
     * 已废弃（不再承接新任务，但保留历史数据）
     */
    DEPRECATED("已废弃");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }
}
