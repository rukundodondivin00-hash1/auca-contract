package com.auca.contractsystem.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PenaltyHistoryDto {
    private String id;
    private String installmentId;
    private BigDecimal previousAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal newAmount;
    private String reason;
    private LocalDateTime createdAt;
}
