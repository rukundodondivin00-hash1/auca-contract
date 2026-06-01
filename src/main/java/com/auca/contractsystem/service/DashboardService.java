package com.auca.contractsystem.service;

import com.auca.contractsystem.client.AucaApiClient;
import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.entity.*;
import com.auca.contractsystem.repository.*;
import com.auca.contractsystem.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final AucaApiClient aucaApiClient;
    private final ContractRepository contractRepository;
    private final InstallmentRepository installmentRepository;
    private final PenaltyRepository penaltyRepository;
    private final BalanceCalculator balanceCalculator;
    private final EligibilityChecker eligibilityChecker;

    public DashboardResponse getDashboard(String studentId) {
        log.info("Building dashboard for student: {}", studentId);

        // Step 1: Get active term
        AucaTermResponse term = aucaApiClient.getActiveTerm();

        // Step 2: Get registration info (totalFees, studentName, etc.)
        AucaRegistrationResponse registration = aucaApiClient.getRegistration(studentId, term.getId());

        // Step 3: Get balance
        AucaBalanceResponse balanceResponse = aucaApiClient.getBalance(studentId);
        BigDecimal balance = balanceResponse.getBalance();
        BigDecimal totalFees = registration.getTotalFee();

        // Step 4: Calculate
        BigDecimal paidAmount = balanceCalculator.calculatePaidAmount(totalFees, balance);
        BigDecimal remainingAmount = balanceCalculator.calculateRemainingAmount(balance);
        Double paidPercentage = balanceCalculator.calculatePaidPercentage(paidAmount, totalFees);
        Double remainingPercentage = balanceCalculator.calculateRemainingPercentage(paidPercentage);

        // Step 5: Determine eligibility
        boolean eligible = eligibilityChecker.isEligible(balance, paidPercentage);
        String eligibilityMessage = eligibilityChecker.getEligibilityMessage(balance, paidPercentage);

        // Step 6: Fetch contract from our DB
        Optional<Contract> contractOpt = contractRepository.findByStudentIdAndTermId(studentId, term.getId());
        ContractDto contractDto = contractOpt.map(this::toContractDto).orElse(null);

        // Step 7: Fetch installments
        List<InstallmentDto> installmentDtos = new ArrayList<>();
        if (contractOpt.isPresent()) {
            List<ContractInstallment> installments = installmentRepository.findByContractId(contractOpt.get().getId());
            installmentDtos = installments.stream().map(this::toInstallmentDto).collect(Collectors.toList());
        }

        return DashboardResponse.builder()
            .studentId(studentId)
            .studentName(registration.getStudentName())
            .studentDepartment(registration.getStudentDepartment())
            .studentFaculty(registration.getStudentFaculty())
            .studentProgram(registration.getStudentProgram())
            .termId(term.getId())
            .academicYear(term.getYear())
            .semester(term.getSemester())
            .totalFees(totalFees)
            .balance(balance)
            .paidAmount(paidAmount)
            .remainingAmount(remainingAmount)
            .paidPercentage(paidPercentage)
            .remainingPercentage(remainingPercentage)
            .eligible(eligible)
            .eligibilityMessage(eligibilityMessage)
            .contract(contractDto)
            .installments(installmentDtos)
            .build();
    }

    // Admin: get all contracts with dashboard info from our DB only
    public List<ContractDto> getAllContracts() {
        return contractRepository.findAllByOrderByCreatedAtDesc()
            .stream().map(this::toContractDto).collect(Collectors.toList());
    }

    private ContractDto toContractDto(Contract c) {
        return ContractDto.builder()
            .id(c.getId()).studentId(c.getStudentId()).studentName(c.getStudentName())
            .termId(c.getTermId()).academicYear(c.getAcademicYear()).semester(c.getSemester())
            .totalFees(c.getTotalFees()).balanceAtSigning(c.getBalanceAtSigning())
            .amountPaidAtSigning(c.getAmountPaidAtSigning()).remainingAtSigning(c.getRemainingAtSigning())
            .status(c.getStatus().name()).agreed(c.getAgreed()).agreedDate(c.getAgreedDate())
            .createdAt(c.getCreatedAt()).build();
    }

    private InstallmentDto toInstallmentDto(ContractInstallment i) {
        List<PenaltyHistoryDto> penalties = penaltyRepository.findByInstallmentId(i.getId())
            .stream().map(p -> PenaltyHistoryDto.builder()
                .id(p.getId()).installmentId(i.getId())
                .previousAmount(p.getPreviousAmount()).penaltyAmount(p.getPenaltyAmount())
                .newAmount(p.getNewAmount()).reason(p.getReason()).createdAt(p.getCreatedAt())
                .build()).collect(Collectors.toList());

        return InstallmentDto.builder()
            .id(i.getId()).contractId(i.getContract().getId())
            .installmentNumber(i.getInstallmentNumber()).deadlineDate(i.getDeadlineDate())
            .amountDue(i.getAmountDue()).amountPaid(i.getAmountPaid())
            .penaltyAmount(i.getPenaltyAmount()).status(i.getStatus().name())
            .paidAt(i.getPaidAt()).penaltyHistories(penalties).build();
    }
}
