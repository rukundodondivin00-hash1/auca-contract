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
public class AdminContractDto {
    private String id;
    private String studentId;
    private String studentName;
    private String termId;
    private String academicYear;
    private String semester;
    private BigDecimal totalFees;
    private BigDecimal balanceAtSigning;
    private BigDecimal amountPaidAtSigning;
    private BigDecimal remainingAtSigning;
    private String status;
    private Boolean agreed;
    private LocalDate agreedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int installmentCount;
    private BigDecimal totalPaidOnInstallments;
    private BigDecimal totalPenaltyOnInstallments;
}
