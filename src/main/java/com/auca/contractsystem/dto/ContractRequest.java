package com.auca.contractsystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class ContractRequest {
    @NotNull(message = "Installments are required")
    @Size(min = 2, max = 4, message = "You must have between 2 and 4 installments")
    @Valid
    private List<InstallmentRequest> installments;
}
