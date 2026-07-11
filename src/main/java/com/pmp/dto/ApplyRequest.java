package com.pmp.dto;

import com.pmp.enumeration.ProjectType;
import com.pmp.enumeration.RepeatType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ApplyRequest {
    private String name;
    private ProjectType type;
    private BigDecimal unitPrice;
    private RepeatType repeatType;
    private LocalDate endDate;
    private Integer pointsToConsume;
    private String remark;
}
