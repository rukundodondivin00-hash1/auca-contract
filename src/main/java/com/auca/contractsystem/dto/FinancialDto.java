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
public class FinancialDto {
    private BigDecimal totalFees;
    private BigDecimal paidAmount;
    private BigDecimal remainingBalance;
    private BigDecimal paymentPercentage;
    private Boolean isEligibleForContract;
}