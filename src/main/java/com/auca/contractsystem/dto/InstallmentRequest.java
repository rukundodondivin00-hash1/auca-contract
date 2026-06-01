package com.auca.contractsystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InstallmentRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Deadline date is required")
    @Future(message = "Deadline must be a future date")
    private LocalDate deadlineDate;
}
