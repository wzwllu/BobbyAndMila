package com.pmp.dto;

import lombok.Data;

@Data
public class TaskCompleteRequest {
    private Long assignmentId;
    private Integer quantity;
    private String remark;
}
