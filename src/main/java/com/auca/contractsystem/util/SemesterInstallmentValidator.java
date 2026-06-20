package com.auca.contractsystem.util;

import com.auca.contractsystem.exception.ContractException;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;

@Component
public class SemesterInstallmentValidator {

    public void validateInstallments(String termId, int academicYear, List<LocalDate> deadlines) {
        int semester = extractSemester(termId);
        
        int expectedCount = getExpectedInstallmentCount(semester);
        if (deadlines.size() != expectedCount) {
            throw new ContractException(
                "Semester " + semester + " requires " + expectedCount + " installments, but " + deadlines.size() + " were provided.");
        }
        
        validateDeadlines(semester, academicYear, deadlines);
    }

    public int getExpectedInstallmentCount(String termId) {
        return getExpectedInstallmentCount(extractSemester(termId));
    }

    private int getExpectedInstallmentCount(int semester) {
        return switch (semester) {
            case 1 -> 2;
            case 2 -> 3;
            default -> throw new ContractException("Invalid semester: " + semester);
        };
    }

    private void validateDeadlines(int semester, int academicYear, List<LocalDate> deadlines) {
        int deadlineYear = (semester == 1) ? academicYear : academicYear + 1;
        
        List<Month> expectedMonths = switch (semester) {
            case 1 -> List.of(Month.OCTOBER, Month.NOVEMBER);
            case 2 -> List.of(Month.FEBRUARY, Month.MARCH, Month.APRIL);
            default -> throw new ContractException("Invalid semester: " + semester);
        };

        for (int i = 0; i < deadlines.size(); i++) {
            LocalDate deadline = deadlines.get(i);
            Month expectedMonth = expectedMonths.get(i);
            
            if (deadline.getYear() != deadlineYear) {
                throw new ContractException(
                    "Installment " + (i + 1) + " deadline must be in year " + deadlineYear + ".");
            }
            
            // FIX: Safely calculate the last day of the month, accounting for leap years automatically
            LocalDate expectedLastDay = YearMonth.of(deadlineYear, expectedMonth).atEndOfMonth();
            
            if (!deadline.equals(expectedLastDay)) {
                throw new ContractException(
                    "Installment " + (i + 1) + " deadline must be the last day of " + expectedMonth + " (" + expectedLastDay + ").");
            }
            
            if (deadline.isBefore(LocalDate.now())) {
                throw new ContractException(
                    "Installment " + (i + 1) + " deadline must not be in the past.");
            }
        }
    }

    public int extractSemester(String termId) {
        if (termId == null || !termId.matches("\\d{4}/\\d")) {
            throw new ContractException("Invalid term ID format: " + termId + ". Expected format: YYYY/S");
        }
        return Integer.parseInt(termId.substring(5));
    }
}