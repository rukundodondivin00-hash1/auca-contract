package com.auca.contractsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PaymentResponseDto {
    private String contractId;
    private BigDecimal totalAmountPaid;
    private List<InstallmentUpdateDto> installmentUpdates;
    
    @Data
    @AllArgsConstructor
    public static class InstallmentUpdateDto {
        private String installmentId;
        private Integer installmentNumber;
        private BigDecimal amountPaid;
        private String status;
        private Boolean fullyPaid;
    }
}