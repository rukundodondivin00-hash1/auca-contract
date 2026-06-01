package com.auca.contractsystem.util;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BalanceCalculator {

    public BigDecimal calculatePaidAmount(BigDecimal totalFees, BigDecimal balance) {
        // balance is negative when student owes money
        // paidAmount = totalFees - abs(balance)
        BigDecimal remaining = balance.abs();
        return totalFees.subtract(remaining).max(BigDecimal.ZERO);
    }

    public BigDecimal calculateRemainingAmount(BigDecimal balance) {
        // remaining = absolute value of balance (when negative)
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            return balance.abs();
        }
        return BigDecimal.ZERO;
    }

    public Double calculatePaidPercentage(BigDecimal paidAmount, BigDecimal totalFees) {
        if (totalFees.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return paidAmount.divide(totalFees, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    public Double calculateRemainingPercentage(Double paidPercentage) {
        return Math.max(0.0, 100.0 - paidPercentage);
    }
}
