package com.auca.contractsystem.dto.admin;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminInstallmentDto {
    private String id;
    private String contractId;
    private Integer installmentNumber;
    private LocalDate deadlineDate;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private BigDecimal penaltyAmount;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String studentName;
    private String termId;
}
