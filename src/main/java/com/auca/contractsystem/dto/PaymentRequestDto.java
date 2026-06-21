package com.auca.contractsystem.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequestDto {
    private BigDecimal amount;
    private String channel;
    private String phoneNumber;
}