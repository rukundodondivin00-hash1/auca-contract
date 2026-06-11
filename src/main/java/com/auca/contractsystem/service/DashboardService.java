package com.auca.contractsystem.service;

import com.auca.contractsystem.client.AucaApiClient;
import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.entity.Contract;
import com.auca.contractsystem.entity.ContractInstallment;
import com.auca.contractsystem.exception.AucaApiException;
import com.auca.contractsystem.repository.ContractRepository;
import com.auca.contractsystem.repository.InstallmentRepository;
import com.auca.contractsystem.util.BalanceCalculator;
import com.auca.contractsystem.util.EligibilityChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final AucaApiClient aucaApiClient;
    private final ContractRepository contractRepository;
    private final InstallmentRepository installmentRepository;
    private final BalanceCalculator balanceCalculator;
    private final EligibilityChecker eligibilityChecker;

    public UnifiedDashboardResponse getUnifiedDashboard(String studentId) {
        log.info("Building unified dashboard for student: {}", studentId);

        AucaTermResponse term = safeCall(() -> aucaApiClient.getActiveTerm(), "active term");
        AucaRegistrationResponse registration = safeCall(
            () -> aucaApiClient.getRegistration(studentId, term != null ? term.getId() : null),
            "registration"
        );
        AucaStudentDashboardResponse studentDashboard = safeCall(
            () -> aucaApiClient.getStudentDashboard(studentId), "student dashboard");
        AucaBalanceResponse balanceResponse = safeCall(
            () -> aucaApiClient.getBalance(studentId), "balance");

        StudentDto studentDto = null;
        if (registration != null) {
            studentDto = StudentDto.builder()
                .id(studentId)
                .name(registration.getStudentName())
                .email(null)
                .department(registration.getStudentDepartment())
                .departmentCode(registration.getStudentDepartmentCode())
                .program(registration.getStudentProgram())
                .build();
        }

        AcademicDto academicDto = null;
        if (term != null && studentDashboard != null) {
            Integer semesterNumber = mapSemesterToNumber(term.getSemester());
            academicDto = AcademicDto.builder()
                .activeTerm(term.getId())
                .semesterNumber(semesterNumber)
                .semesterName(term.getSemester())
                .credits(studentDashboard.getGpaData() != null ? studentDashboard.getGpaData().getTotalCreditsEarned() : 0)
                .cumulativeGpa(studentDashboard.getGpaData() != null ? studentDashboard.getGpaData().getCumulativeGPA() : null)
                .registeredCredits(studentDashboard.getRegistrationSummary() != null
                    ? studentDashboard.getRegistrationSummary().getRegisteredCredits()
                    : 0)
                .build();
        }

        FinancialDto financialDto = null;
        if (registration != null && balanceResponse != null) {
            BigDecimal balance = balanceResponse.getBalance();
            BigDecimal totalFees = registration.getTotalFee();
            BigDecimal paidAmount = balanceCalculator.calculatePaidAmount(totalFees, balance);
            BigDecimal remainingAmount = balanceCalculator.calculateRemainingAmount(balance);
            Double paidPercentage = balanceCalculator.calculatePaidPercentage(paidAmount, totalFees);
            boolean eligible = eligibilityChecker.isEligible(balance, paidPercentage);

            financialDto = FinancialDto.builder()
                .totalFees(totalFees)
                .paidAmount(paidAmount)
                .remainingBalance(remainingAmount)
                .paymentPercentage(BigDecimal.valueOf(paidPercentage).setScale(2, RoundingMode.HALF_UP))
                .isEligibleForContract(eligible)
                .build();
        }

        ContractResponseDto contractDto = null;
        if (term != null) {
            Optional<Contract> contractOpt = contractRepository.findByStudentIdAndTermId(studentId, term.getId());
            contractDto = contractOpt.map(this::toContractResponseDto).orElse(
                ContractResponseDto.builder().hasContract(false).installments(Collections.emptyList()).build());
        }

        RegistrationDto registrationDto = null;
        if (registration != null) {
            registrationDto = RegistrationDto.builder()
                .isRegistrationOpen(Boolean.TRUE.equals(registration.getIsRegistrationOpen()))
                .courses(registration.getCourses() != null
                    ? registration.getCourses().stream().map(this::toCourseDto).collect(Collectors.toList())
                    : Collections.emptyList())
                .build();
        }

        return UnifiedDashboardResponse.builder()
            .student(studentDto)
            .academic(academicDto)
            .financial(financialDto)
            .contract(contractDto)
            .registration(registrationDto)
            .build();
    }

    private <T> T safeCall(java.util.concurrent.Callable<T> callable, String name) {
        try {
            return callable.call();
        } catch (Exception e) {
            log.warn("AUCA {} fetch failed: {}", name, e.getMessage());
            return null;
        }
    }

    private Integer mapSemesterToNumber(String semester) {
        if (semester == null) return 0;
        switch (semester.toUpperCase()) {
            case "SEPTEMBER": return 1;
            case "JANUARY": return 2;
            case "JUNE": return 3;
            default: return 0;
        }
    }

    private CourseDto toCourseDto(AucaRegistrationResponse.CourseItem item) {
        return CourseDto.builder()
            .code(item.getCourseCode())
            .name(item.getCourseName())
            .credits(item.getCredits())
            .day(item.getDay())
            .startTime(item.getStartTime())
            .endTime(item.getEndTime())
            .room(item.getRoomName())
            .build();
    }

    private ContractResponseDto toContractResponseDto(Contract c) {
        List<InstallmentResponseDto> installments = installmentRepository.findByContractId(c.getId())
            .stream().map(this::toInstallmentResponseDto).collect(Collectors.toList());

        return ContractResponseDto.builder()
            .hasContract(true)
            .installments(installments)
            .build();
    }

    private InstallmentResponseDto toInstallmentResponseDto(ContractInstallment i) {
        return InstallmentResponseDto.builder()
            .id(i.getId())
            .amount(i.getAmountDue())
            .deadlineDate(i.getDeadlineDate() != null ? i.getDeadlineDate().toString() : null)
            .status(i.getStatus().name())
            .build();
    }
}
