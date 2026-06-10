package com.auca.contractsystem.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractResponseDto {
    private Boolean hasContract;
    private List<InstallmentResponseDto> installments;
}