package com.pmp.dto.redemption;

import lombok.Data;

@Data
public class ReviewRedemptionRequest {
    private Boolean approved;
    private String reviewRemark;
}
