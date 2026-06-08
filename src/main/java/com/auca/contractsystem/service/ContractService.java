package com.auca.contractsystem.service;

import com.auca.contractsystem.client.AucaApiClient;
import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.entity.*;
import com.auca.contractsystem.exception.*;
import com.auca.contractsystem.repository.*;
import com.auca.contractsystem.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final AucaApiClient aucaApiClient;
    private final ContractRepository contractRepository;
    private final InstallmentRepository installmentRepository;
    private final BalanceCalculator balanceCalculator;
    private final EligibilityChecker eligibilityChecker;
    private final SemesterInstallmentValidator semesterInstallmentValidator;

    @Transactional
    public ContractDto createContract(String studentId, ContractRequest request) {
        log.info("Creating contract for student: {}", studentId);

        // Fetch live data from AUCA
        AucaTermResponse term = aucaApiClient.getActiveTerm();
        AucaRegistrationResponse registration = aucaApiClient.getRegistration(studentId, term.getId());
        AucaBalanceResponse balanceResponse = aucaApiClient.getBalance(studentId);

        BigDecimal balance = balanceResponse.getBalance();
        BigDecimal totalFees = registration.getTotalFee();
        BigDecimal paidAmount = balanceCalculator.calculatePaidAmount(totalFees, balance);
        BigDecimal remainingAmount = balanceCalculator.calculateRemainingAmount(balance);
        Double paidPercentage = balanceCalculator.calculatePaidPercentage(paidAmount, totalFees);

        // Check eligibility
        if (!eligibilityChecker.isEligible(balance, paidPercentage)) {
            throw new ContractException("You are not eligible for a contract. You must pay at least 50% of your total fees.");
        }

        // Check if contract already exists for this term
        contractRepository.findByStudentIdAndTermId(studentId, term.getId()).ifPresent(c -> {
            throw new ContractException("A contract already exists for this term.");
        });

        // Validate installments sum equals remaining balance
        BigDecimal installmentTotal = request.getInstallments().stream()
            .map(InstallmentRequest::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (installmentTotal.compareTo(remainingAmount) != 0) {
            throw new ContractException(
                "Total installment amounts (" + installmentTotal + ") must equal your remaining balance (" + remainingAmount + ").");
        }

        // Validate semester-based installment count and deadlines
        List<LocalDate> deadlines = request.getInstallments().stream()
            .map(InstallmentRequest::getDeadlineDate)
            .toList();
        int academicYear = Integer.parseInt(term.getYear());
        semesterInstallmentValidator.validateInstallments(term.getId(), academicYear, deadlines);

        // Create contract
        Contract contract = Contract.builder()
            .studentId(studentId)
            .studentName(registration.getStudentName())
            .termId(term.getId())
            .academicYear(term.getYear())
            .semester(term.getSemester())
            .totalFees(totalFees)
            .balanceAtSigning(balance)
            .amountPaidAtSigning(paidAmount)
            .remainingAtSigning(remainingAmount)
            .status(Contract.ContractStatus.ACTIVE)
            .agreed(true)
            .agreedDate(LocalDate.now())
            .build();

        Contract saved = contractRepository.save(contract);

        // Save installments
        for (int i = 0; i < request.getInstallments().size(); i++) {
            InstallmentRequest ir = request.getInstallments().get(i);
            ContractInstallment installment = ContractInstallment.builder()
                .contract(saved)
                .installmentNumber(i + 1)
                .deadlineDate(ir.getDeadlineDate())
                .amountDue(ir.getAmount())
                .amountPaid(BigDecimal.ZERO)
                .penaltyAmount(BigDecimal.ZERO)
                .status(ContractInstallment.InstallmentStatus.PENDING)
                .build();
            installmentRepository.save(installment);
        }

        log.info("Contract created successfully for student: {}", studentId);
        return toContractDto(saved);
    }

    public List<ContractDto> getStudentContracts(String studentId) {
        return contractRepository.findByStudentId(studentId)
            .stream().map(this::toContractDto).toList();
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
}
