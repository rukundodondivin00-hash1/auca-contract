package com.auca.contractsystem.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentDto {
    private String id;
    private String contractId;
    private Integer installmentNumber;
    private String deadlineDate;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private String status;
    private BigDecimal penaltyAmount;
}