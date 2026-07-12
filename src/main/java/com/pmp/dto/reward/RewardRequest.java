package com.pmp.dto.reward;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;

@Data
public class RewardRequest {
    @NotBlank(message = "奖励名称不能为空")
    @Size(max = 100)
    private String name;

    @NotNull(message = "所需积分不能为空")
    @Positive(message = "所需积分必须大于 0")
    private Integer costPoints;

    @Size(max = 500)
    private String description;

    private String imageUrl;

    @Min(1)
    private Integer stock;
}
