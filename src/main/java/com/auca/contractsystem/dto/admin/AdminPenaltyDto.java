package com.auca.contractsystem.dto.admin;

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
public class AdminPenaltyDto {
    private String id;
    private String installmentId;
    private String contractId;
    private String studentName;
    private BigDecimal previousAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal newAmount;
    private String reason;
    private LocalDateTime createdAt;
}
