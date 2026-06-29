package com.auca.contractsystem.dto.staff;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StaffPenaltyDto {
    private String id;
    private String installmentId;
    private String contractId;
    private String studentId;
    private String studentName;
    private BigDecimal previousAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal newAmount;
    private String reason;
    private LocalDateTime createdAt;
}
