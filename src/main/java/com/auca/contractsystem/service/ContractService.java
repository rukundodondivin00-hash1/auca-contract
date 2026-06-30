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
    private final TermConfigRepository termConfigRepository;
    private final PenaltyRepository penaltyRepository;

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

        // ── 3. Get TermConfig for minimum required check ────────────────────────
        TermConfig termConfig = termConfigRepository.findByTermId(term.getId())
            .orElseThrow(() -> new ContractException("No contract configuration found for this term."));

        // ── 4. Check eligibility using PrePayment records ──────────────────────
        BigDecimal paidAmount = prePaymentRepository.sumAmountByStudentId(studentId);
        
        BigDecimal initialPaymentPercentage = termConfig.getInitialPaymentPercentage() != null 
            ? termConfig.getInitialPaymentPercentage() 
            : new BigDecimal("100.00");
            
        BigDecimal minimumRequired = totalFees.multiply(initialPaymentPercentage)
            .divide(new BigDecimal("100"), 0, java.math.RoundingMode.HALF_UP);
            
        if (paidAmount.compareTo(minimumRequired) < 0) {
            BigDecimal shortfall = minimumRequired.subtract(paidAmount);
            throw new ContractException(
                "You must pay at least " + initialPaymentPercentage + "% of your total fees (" + minimumRequired.toPlainString() + " RWF) " +
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

        // ── 6. Determine number of installments ──────────────────────────────
        List<TermInstallmentConfig> configInstallments = termConfig.getInstallments();
        if (configInstallments == null || configInstallments.isEmpty()) {
            // If the staff configured 100% initial payment, no installments are needed.
            // But if initial payment is < 100% and there are no installments, it's an error.
            if (initialPaymentPercentage.compareTo(new BigDecimal("100")) < 0) {
                throw new ContractException("Term configuration has no installments set. Please contact the registrar.");
            }
        }
        
        int requestedInstallments = configInstallments != null ? configInstallments.size() : 0;
        if (request != null && request.getInstallments() != null) {
            requestedInstallments = request.getInstallments().size();
            if (configInstallments != null && requestedInstallments > configInstallments.size()) {
                throw new ContractException("You cannot select more than " + configInstallments.size() + " installments.");
            }
            if (requestedInstallments < 1 && initialPaymentPercentage.compareTo(new BigDecimal("100")) < 0) {
                throw new ContractException("You must select at least 1 installment.");
            }
        }
        
        // Parse year/semester from termId
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
        BigDecimal totalAllocated = BigDecimal.ZERO;
        // Check if custom amounts were provided and validate their sum
        BigDecimal sumRequestedAmounts = BigDecimal.ZERO;
        boolean useRequestedAmounts = false;
        
        if (request != null && request.getInstallments() != null && request.getInstallments().size() == requestedInstallments) {
            for (com.auca.contractsystem.dto.InstallmentRequest reqInst : request.getInstallments()) {
                if (reqInst.getAmount() != null) {
                    sumRequestedAmounts = sumRequestedAmounts.add(reqInst.getAmount());
                }
            }
            if (sumRequestedAmounts.compareTo(remainingAmount) == 0) {
                useRequestedAmounts = true;
            } else if (sumRequestedAmounts.compareTo(BigDecimal.ZERO) > 0) {
                throw new ContractException("The sum of your custom installments (" + sumRequestedAmounts + ") does not equal the remaining contract balance (" + remainingAmount + ").");
            }
        }
        
        for (int i = 0; i < requestedInstallments; i++) {
            TermInstallmentConfig tic = configInstallments.get(i);
            
            BigDecimal amountDue;
            LocalDate deadlineDate = tic.getDeadlineDate();
            
            if (request != null && request.getInstallments() != null && request.getInstallments().size() == requestedInstallments) {
                com.auca.contractsystem.dto.InstallmentRequest reqInst = request.getInstallments().get(i);
                if (useRequestedAmounts && reqInst.getAmount() != null) {
                    amountDue = reqInst.getAmount();
                } else {
                    if (i == requestedInstallments - 1) {
                        amountDue = remainingAmount.subtract(totalAllocated);
                    } else {
                        if (requestedInstallments < configInstallments.size()) {
                            amountDue = remainingAmount.divide(BigDecimal.valueOf(requestedInstallments), 0, java.math.RoundingMode.HALF_UP);
                        } else {
                            amountDue = totalFees.multiply(tic.getPercentage()).divide(new BigDecimal("100"), 0, java.math.RoundingMode.HALF_UP);
                            BigDecimal leftToAllocate = remainingAmount.subtract(totalAllocated);
                            if (amountDue.compareTo(leftToAllocate) > 0) amountDue = leftToAllocate;
                            if (amountDue.compareTo(BigDecimal.ZERO) < 0) amountDue = BigDecimal.ZERO;
                        }
                    }
                }
                totalAllocated = totalAllocated.add(amountDue);
                
                // Use requested deadline date
                if (reqInst.getDeadlineDate() != null) {
                    deadlineDate = reqInst.getDeadlineDate();
                    
                    if (deadlineDate.isBefore(LocalDate.now())) {
                        throw new ContractException("Deadline date for installment " + (i + 1) + " cannot be in the past.");
                    }
                    
                    LocalDate finalDeadline = configInstallments.get(configInstallments.size() - 1).getDeadlineDate();
                    if (finalDeadline != null && deadlineDate.isAfter(finalDeadline)) {
                        throw new ContractException("Deadline date for installment " + (i + 1) + " cannot be after the final term deadline (" + finalDeadline + ").");
                    }
                }
            } else {
                if (i == requestedInstallments - 1) {
                    amountDue = remainingAmount.subtract(totalAllocated);
                } else {
                    if (requestedInstallments < configInstallments.size()) {
                        amountDue = remainingAmount.divide(BigDecimal.valueOf(requestedInstallments), 0, java.math.RoundingMode.HALF_UP);
                    } else {
                        amountDue = totalFees.multiply(tic.getPercentage()).divide(new BigDecimal("100"), 0, java.math.RoundingMode.HALF_UP);
                        BigDecimal leftToAllocate = remainingAmount.subtract(totalAllocated);
                        if (amountDue.compareTo(leftToAllocate) > 0) amountDue = leftToAllocate;
                        if (amountDue.compareTo(BigDecimal.ZERO) < 0) amountDue = BigDecimal.ZERO;
                    }
                    totalAllocated = totalAllocated.add(amountDue);
                }
            }
            
            ContractInstallment installment = ContractInstallment.builder()
                .contract(saved)
                .installmentNumber(tic.getInstallmentNumber())
                .deadlineDate(deadlineDate)
                .amountDue(amountDue)
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

    public List<com.auca.contractsystem.dto.staff.StaffPenaltyDto> getStudentPenalties(String studentId) {
        return penaltyRepository.findByInstallment_Contract_StudentId(studentId)
            .stream()
            .map(p -> com.auca.contractsystem.dto.staff.StaffPenaltyDto.builder()
                .id(p.getId())
                .installmentId(p.getInstallment() != null ? p.getInstallment().getId() : null)
                .contractId(p.getInstallment() != null && p.getInstallment().getContract() != null ? p.getInstallment().getContract().getId() : null)
                .studentName(p.getInstallment() != null && p.getInstallment().getContract() != null ? p.getInstallment().getContract().getStudentName() : null)
                .previousAmount(p.getPreviousAmount())
                .penaltyAmount(p.getPenaltyAmount())
                .newAmount(p.getNewAmount())
                .reason(p.getReason())
                .createdAt(p.getCreatedAt())
                .build())
            .toList();
    }
}