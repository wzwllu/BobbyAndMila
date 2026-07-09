package com.pmp.enumeration;
import lombok.Getter;

@Getter
public enum ProjectType {
    EARN("增加积分"),
    CONSUME("消耗积分");

    private final String label;

    ProjectType(String label) {
        this.label = label;
    }
}
