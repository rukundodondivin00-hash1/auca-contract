package com.auca.contractsystem.service;

import com.auca.contractsystem.client.AucaApiClient;
import com.auca.contractsystem.entity.Contract;
import com.auca.contractsystem.entity.ContractInstallment;
import com.auca.contractsystem.repository.ContractRepository;
import com.auca.contractsystem.repository.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final AucaApiClient aucaApiClient;
    private final ContractRepository contractRepository;
    private final InstallmentRepository installmentRepository;

    @Transactional
    public void processPayment(String studentId, BigDecimal paymentAmount) {
        try {
            aucaApiClient.sendPaymentToBank(studentId, paymentAmount);
        } catch (Exception e) {
            log.error("Bank service unreachable for student {}: {}", studentId, e.getMessage());
            throw new RuntimeException("BANK_OFFLINE"); 
        }

        List<Contract> activeContracts = contractRepository.findByStudentIdAndStatus(
            studentId, Contract.ContractStatus.ACTIVE);
        
        if (activeContracts.isEmpty()) {
            log.warn("No ACTIVE contract found for student {}", studentId);
            return;
        }

        Contract activeContract = activeContracts.get(0);
        log.info("Processing payment of {} for student {} on contract {}", paymentAmount, studentId, activeContract.getId());

        List<ContractInstallment> unpaidInstallments = installmentRepository
            .findByContractIdAndStatusInPENDING_OR_PARTIALLY_PAID(activeContract.getId());
        
        log.info("Found {} unpaid installments for contract {}", unpaidInstallments.size(), activeContract.getId());

        BigDecimal remainingPayment = paymentAmount;

        for (ContractInstallment installment : unpaidInstallments) {
            if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal currentlyPaid = installment.getAmountPaid() != null ? installment.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal amountNeededToClear = installment.getAmountDue().subtract(currentlyPaid);

            if (remainingPayment.compareTo(amountNeededToClear) >= 0) {
                installment.setAmountPaid(installment.getAmountDue());
                installment.setStatus(ContractInstallment.InstallmentStatus.PAID);
                installment.setPaidAt(LocalDateTime.now());
                remainingPayment = remainingPayment.subtract(amountNeededToClear);
            } else {
                installment.setAmountPaid(currentlyPaid.add(remainingPayment));
                installment.setStatus(ContractInstallment.InstallmentStatus.PARTIALLY_PAID);
                remainingPayment = BigDecimal.ZERO;
            }
            installmentRepository.save(installment);
        }

        List<ContractInstallment> remainingUnpaid = installmentRepository
            .findByContractIdAndStatusNotOrderByDeadlineDateAsc(activeContract.getId(), ContractInstallment.InstallmentStatus.PAID);

        if (remainingUnpaid.isEmpty()) {
            activeContract.setStatus(Contract.ContractStatus.COMPLETED);
            contractRepository.save(activeContract);
        }
    }
}