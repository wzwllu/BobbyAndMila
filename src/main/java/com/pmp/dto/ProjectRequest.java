package com.pmp.dto;

import com.pmp.enumeration.ProjectType;
import com.pmp.enumeration.RepeatType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjectRequest {
    private String name;
    private ProjectType type;
    private BigDecimal unitPrice;
    private RepeatType repeatType;
    private Integer repeatDay;
    private Integer pointsToConsume;
}
