package com.auca.contractsystem.service;

import com.auca.contractsystem.client.AucaApiClient;
import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.entity.*;
import com.auca.contractsystem.exception.*;
import com.auca.contractsystem.repository.*;
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
    private final PrePaymentRepository prePaymentRepository;

    @Transactional
    public ContractDto createContract(String studentId, ContractRequest request) {
        log.info("Creating contract for student: {}", studentId);
        try {
            return createContractInternal(studentId, request);
        } catch (AucaApiException e) {
            log.error("AUCA service unavailable during contract creation: {}", e.getMessage());
            throw new ContractException("Cannot create contract: AUCA service is currently unavailable. Please try again later.");
        }
    }

    @Transactional
    public ContractDto createContractInternal(String studentId, ContractRequest request) {

        // ── 1. Get the open term from IMS ─────────────────────────────────────
        AucaTermResponse term = aucaApiClient.getActiveTerm();
        if (term == null) {
            throw new ContractException("No open registration term found. Please check with the registrar.");
        }

        // ── 2. Get the student's registration (courses + totalFee) from IMS ───
        // Try my-registration first (header-based), then fall back to explicit params
        AucaRegistrationResponse registration = aucaApiClient.getMyRegistration(studentId);
        if (registration == null) {
            registration = aucaApiClient.getRegistration(studentId, term.getId());
        }
        if (registration == null) {
            throw new ContractException("No registration found for student " + studentId +
                " in term " + term.getId() + ". Please register for courses first.");
        }

        BigDecimal totalFees = registration.getTotalFee();
        if (totalFees == null || totalFees.compareTo(BigDecimal.ZERO) == 0) {
            throw new ContractException("No fee found for your registration. Please contact the registrar.");
        }

        // ── 3. Check 50% eligibility using PrePayment records ─────────────────
        BigDecimal paidAmount = prePaymentRepository.sumAmountByStudentId(studentId);
        BigDecimal minimumRequired = totalFees.divide(BigDecimal.valueOf(2));
        if (paidAmount.compareTo(minimumRequired) < 0) {
            BigDecimal shortfall = minimumRequired.subtract(paidAmount);
            throw new ContractException(
                "You must pay at least 50% of your total fees (" + minimumRequired.toPlainString() + " RWF) " +
                "before signing a contract. You have paid " + paidAmount.toPlainString() + " RWF. " +
                "Please pay " + shortfall.toPlainString() + " RWF more first.");
        }

        // ── 4. Prevent duplicate contracts ────────────────────────────────────
        contractRepository.findByStudentIdAndTermId(studentId, term.getId()).ifPresent(c -> {
            throw new ContractException("A contract already exists for this term.");
        });

        BigDecimal remainingAmount = totalFees.subtract(paidAmount);
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ContractException("Your fees are fully paid. No contract is needed.");
        }

        // ── 5. Validate installment total matches remaining balance ────────────
        BigDecimal installmentTotal = request.getInstallments().stream()
            .map(InstallmentRequest::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (installmentTotal.compareTo(remainingAmount) != 0) {
            throw new ContractException(
                "Total installment amounts (" + installmentTotal.toPlainString() +
                ") must equal your remaining balance (" + remainingAmount.toPlainString() + " RWF).");
        }

        // ── 6. Validate installment deadlines for this semester ───────────────
        List<LocalDate> deadlines = request.getInstallments().stream()
            .map(InstallmentRequest::getDeadlineDate)
            .toList();
        // Parse year/semester from termId ("2025/1") since IMS may not return separate fields
        String rawTermId = term.getId() != null ? term.getId() : "";
        int academicYear = java.time.Year.now().getValue();
        String termSemester = "1";
        if (rawTermId.contains("/")) {
            String[] parts = rawTermId.split("/");
            try { academicYear = Integer.parseInt(parts[0].trim()); } catch (NumberFormatException ignored) {}
            termSemester = parts.length > 1 ? parts[1].trim() : "1";
        } else if (term.getYear() != null) {
            try { academicYear = Integer.parseInt(term.getYear()); } catch (NumberFormatException ignored) {}
            termSemester = term.getSemester() != null ? term.getSemester() : "1";
        }
        validateInstallmentDeadlines(termSemester, academicYear, deadlines);

        // ── 7. Build and save the contract ────────────────────────────────────
        Contract contract = Contract.builder()
            .studentId(studentId)
            .studentName(registration.getStudentName() != null ? registration.getStudentName() : studentId)
            .termId(term.getId())
            .academicYear(String.valueOf(academicYear))
            .semester(termSemester)
            .totalFees(totalFees)
            .balanceAtSigning(remainingAmount)
            .amountPaidAtSigning(paidAmount)
            .remainingAtSigning(remainingAmount)
            .status(Contract.ContractStatus.ACTIVE)
            .agreed(true)
            .agreedDate(LocalDate.now())
            .build();

        Contract saved = contractRepository.save(contract);

        java.util.List<ContractInstallment> savedInstallments = new java.util.ArrayList<>();
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
            savedInstallments.add(installmentRepository.save(installment));
        }
        saved.setInstallments(savedInstallments);

        log.info("Contract created successfully for student: {} in term: {}", studentId, term.getId());
        return toContractDto(saved);
    }

    private void validateInstallmentDeadlines(String semester, int year, List<LocalDate> deadlines) {
        int semNum;
        try { semNum = Integer.parseInt(semester); } catch (NumberFormatException e) { semNum = 1; }
        // Semester 3 = no contract allowed
        if (semNum == 3) {
            throw new ContractException("No contract is allowed for the summer semester (Semester 3).");
        }
        int expectedCount = semNum == 1 ? 2 : 3;
        if (deadlines.size() != expectedCount) {
            throw new ContractException("Semester " + semNum + " requires exactly " + expectedCount + " installments.");
        }
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
            .createdAt(c.getCreatedAt())
            .installments(c.getInstallments() != null
                ? c.getInstallments().stream().map(this::toInstallmentDto).toList()
                : java.util.Collections.emptyList())
            .build();
    }

    private InstallmentDto toInstallmentDto(ContractInstallment i) {
        return InstallmentDto.builder()
            .id(i.getId())
            .contractId(i.getContract() != null ? i.getContract().getId() : null)
            .installmentNumber(i.getInstallmentNumber())
            .deadlineDate(i.getDeadlineDate() != null ? i.getDeadlineDate().toString() : null)
            .amountDue(i.getAmountDue())
            .amountPaid(i.getAmountPaid())
            .status(i.getStatus().name())
            .penaltyAmount(i.getPenaltyAmount())
            .build();
    }
}