package com.auca.contractsystem.dto.admin;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminStudentSummaryDto {
    private String studentId;
    private String studentName;
    private String department;
    private String departmentCode;
    private String program;
    private int contractCount;
    private BigDecimal totalFeesAcrossContracts;
    private BigDecimal totalPaidAcrossContracts;
    private BigDecimal totalRemainingAcrossContracts;
    private boolean hasActiveContract;
}
