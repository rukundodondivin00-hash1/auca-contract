package com.auca.contractsystem.dto;

import com.auca.contractsystem.entity.ContractInstallment;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstallmentResponse {
    private String id;
    private String contractId;
    private Integer installmentNumber;
    private LocalDate deadlineDate;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private BigDecimal penaltyAmount;
    private BigDecimal totalDue;
    private ContractInstallment.InstallmentStatus status;
    private LocalDateTime paidAt;
    private boolean overdue;
    private long daysOverdue;
}
