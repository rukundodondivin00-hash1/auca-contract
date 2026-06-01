package com.auca.contractsystem.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDto {
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
}
