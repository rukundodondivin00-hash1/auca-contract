package com.auca.contractsystem.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AucaBalanceResponse {
    private String studentId;
    private BigDecimal balance;
}
