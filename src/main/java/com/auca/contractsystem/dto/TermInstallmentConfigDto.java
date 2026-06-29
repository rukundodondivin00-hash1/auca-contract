package com.auca.contractsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermInstallmentConfigDto {
    private String id;

    @NotNull(message = "Installment number is required")
    private Integer installmentNumber;

    @NotNull(message = "Percentage is required")
    private BigDecimal percentage;

    @NotNull(message = "Deadline date is required")
    private LocalDate deadlineDate;
}
