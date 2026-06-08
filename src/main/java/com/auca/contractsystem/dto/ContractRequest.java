package com.auca.contractsystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class ContractRequest {
    @NotNull(message = "Installments are required")
    @Size(min = 1, message = "At least one installment is required")
    @Valid
    private List<InstallmentRequest> installments;
}
