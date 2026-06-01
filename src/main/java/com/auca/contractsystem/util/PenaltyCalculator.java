package com.auca.contractsystem.util;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PenaltyCalculator {

    private static final BigDecimal PENALTY_RATE = new BigDecimal("0.05"); // 5% per month

    public BigDecimal calculatePenalty(BigDecimal amountDue) {
        return amountDue.multiply(PENALTY_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateNewAmount(BigDecimal amountDue, BigDecimal penalty) {
        return amountDue.add(penalty);
    }
}
