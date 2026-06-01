package com.auca.contractsystem.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private String studentId;
    private String studentName;
    private String studentDepartment;
    private String studentFaculty;
    private String studentProgram;
    private String termId;
    private String academicYear;
    private String semester;
    private BigDecimal totalFees;
    private BigDecimal balance;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private Double paidPercentage;
    private Double remainingPercentage;
    private Boolean eligible;
    private String eligibilityMessage;
    private ContractDto contract;
    private List<InstallmentDto> installments;
}
