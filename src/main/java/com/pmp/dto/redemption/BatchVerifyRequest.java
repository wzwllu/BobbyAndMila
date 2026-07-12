package com.pmp.dto.redemption;

import lombok.Data;

import java.util.List;

@Data
public class BatchVerifyRequest {
    private List<Long> ids;
    private String reviewRemark;
}
