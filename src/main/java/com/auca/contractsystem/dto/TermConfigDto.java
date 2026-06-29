package com.auca.contractsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermConfigDto {
    private String id;
    
    @NotNull(message = "Term ID is required")
    private String termId;
    
    @Builder.Default
    private List<TermInstallmentConfigDto> installments = new ArrayList<>();
    
    @NotNull(message = "Penalty percentage is required")
    private BigDecimal penaltyPercentage;

    @NotNull(message = "Initial payment percentage is required")
    @Builder.Default
    private BigDecimal initialPaymentPercentage = new BigDecimal("100.00");
}
