package com.auca.contractsystem.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstallmentResponseDto {
    private String id;
    private BigDecimal amount;
    private String deadlineDate;
    private String status;
}