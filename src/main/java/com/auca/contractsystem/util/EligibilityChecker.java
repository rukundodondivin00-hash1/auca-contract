package com.auca.contractsystem.util;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class EligibilityChecker {

    private static final double ELIGIBILITY_THRESHOLD = 50.0;

    public boolean isEligible(BigDecimal balance, Double paidPercentage) {
        // balance < 0 means student owes money
        // paidPercentage >= 50% means eligible
        if (balance.compareTo(BigDecimal.ZERO) >= 0) return false; // fully paid or overpaid — no contract needed
        return paidPercentage >= ELIGIBILITY_THRESHOLD;
    }

    public boolean isFullyPaid(BigDecimal balance) {
        return balance.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean isOverpaid(BigDecimal balance) {
        return balance.compareTo(BigDecimal.ZERO) > 0;
    }

    public String getEligibilityMessage(BigDecimal balance, Double paidPercentage) {
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            return "Congratulations! Your financial obligations are fully cleared.";
        }
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            return "Congratulations! You have a credit of RWF " + balance + " on your account.";
        }
        if (paidPercentage >= ELIGIBILITY_THRESHOLD) {
            return "You are eligible to request a payment contract.";
        }
        return "You need to pay at least 50% of your total fees to be eligible for a contract. " +
               "Currently at " + String.format("%.1f", paidPercentage) + "%.";
    }
}
