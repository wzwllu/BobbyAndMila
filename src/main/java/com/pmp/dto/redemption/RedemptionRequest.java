package com.pmp.dto.redemption;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Data
public class RedemptionRequest {
    @NotNull(message = "请选择要兑换的奖励")
    private Long rewardId;

    @Min(value = 1, message = "兑换数量至少为 1")
    private Integer quantity = 1;

    @Size(max = 500)
    private String remark;
}
