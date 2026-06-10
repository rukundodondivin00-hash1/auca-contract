package com.auca.contractsystem.service;

import com.auca.contractsystem.client.AucaApiClient;
import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.entity.Contract;
import com.auca.contractsystem.entity.ContractInstallment;
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

        AucaTermResponse term = aucaApiClient.getActiveTerm();
        AucaRegistrationResponse registration = aucaApiClient.getRegistration(studentId, term.getId());
        AucaStudentDashboardResponse studentDashboard = aucaApiClient.getStudentDashboard(studentId);
        AucaBalanceResponse balanceResponse = aucaApiClient.getBalance(studentId);

        BigDecimal balance = balanceResponse.getBalance();
        BigDecimal totalFees = registration.getTotalFee();
        BigDecimal paidAmount = balanceCalculator.calculatePaidAmount(totalFees, balance);
        BigDecimal remainingAmount = balanceCalculator.calculateRemainingAmount(balance);
        Double paidPercentage = balanceCalculator.calculatePaidPercentage(paidAmount, totalFees);
        boolean eligible = eligibilityChecker.isEligible(balance, paidPercentage);

        Optional<Contract> contractOpt = contractRepository.findByStudentIdAndTermId(studentId, term.getId());
        ContractResponseDto contractDto = contractOpt.map(this::toContractResponseDto).orElse(
            ContractResponseDto.builder().hasContract(false).installments(Collections.emptyList()).build());

        RegistrationDto registrationDto = RegistrationDto.builder()
            .isRegistrationOpen(Boolean.TRUE.equals(registration.getIsRegistrationOpen()))
            .courses(registration.getCourses() != null 
                ? registration.getCourses().stream().map(this::toCourseDto).collect(Collectors.toList())
                : Collections.emptyList())
            .build();

        StudentDto studentDto = StudentDto.builder()
            .id(studentId)
            .name(registration.getStudentName())
            .email(null)
            .department(registration.getStudentDepartment())
            .departmentCode(registration.getStudentDepartmentCode())
            .program(registration.getStudentProgram())
            .build();

        Integer semesterNumber = mapSemesterToNumber(term.getSemester());
        AcademicDto academicDto = AcademicDto.builder()
            .activeTerm(term.getId())
            .semesterNumber(semesterNumber)
            .semesterName(term.getSemester())
            .credits(studentDashboard.getTotalCreditsEarned())
            .cumulativeGpa(studentDashboard.getCumulativeGPA())
            .registeredCredits(studentDashboard.getRegistrationSummary() != null 
                ? studentDashboard.getRegistrationSummary().getRegisteredCredits() 
                : 0)
            .build();

        FinancialDto financialDto = FinancialDto.builder()
            .totalFees(totalFees)
            .paidAmount(paidAmount)
            .remainingBalance(remainingAmount)
            .paymentPercentage(BigDecimal.valueOf(paidPercentage).setScale(2, RoundingMode.HALF_UP))
            .isEligibleForContract(eligible)
            .build();

        return UnifiedDashboardResponse.builder()
            .student(studentDto)
            .academic(academicDto)
            .financial(financialDto)
            .contract(contractDto)
            .registration(registrationDto)
            .build();
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
            .code(item.getCode())
            .name(item.getName())
            .credits(item.getCredits())
            .day(item.getDay())
            .startTime(item.getStartTime())
            .endTime(item.getEndTime())
            .room(item.getRoom())
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
