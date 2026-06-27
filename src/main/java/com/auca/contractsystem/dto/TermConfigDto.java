package com.auca.contractsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermConfigDto {
    private String id;
    
    @NotNull(message = "Term ID is required")
    private String termId;
    
    @NotNull(message = "Max installments is required")
    private Integer maxInstallments;
    
    @NotNull(message = "Penalty percentage is required")
    private BigDecimal penaltyPercentage;
}
