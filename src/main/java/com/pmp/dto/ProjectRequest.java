package com.pmp.dto;

import com.pmp.enumeration.RepeatType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectRequest {
    private String name;
    private BigDecimal unitPrice;
    private RepeatType repeatType;
    private LocalDate endDate;
}
