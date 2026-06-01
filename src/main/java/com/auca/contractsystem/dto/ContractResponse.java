package com.auca.contractsystem.dto;

import com.auca.contractsystem.entity.Contract;
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
public class ContractResponse {
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
    private Contract.ContractStatus status;
    private Boolean agreed;
    private LocalDate agreedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
