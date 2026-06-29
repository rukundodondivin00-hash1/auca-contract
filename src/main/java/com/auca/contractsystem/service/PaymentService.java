package com.auca.contractsystem.service;

import com.auca.contractsystem.dto.PaymentResponseDto;
import com.auca.contractsystem.entity.Contract;
import com.auca.contractsystem.entity.ContractInstallment;
import com.auca.contractsystem.exception.ContractException;
import com.auca.contractsystem.repository.ContractRepository;
import com.auca.contractsystem.repository.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.auca.contractsystem.dto.NotificationMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final ContractRepository contractRepository;
    private final InstallmentRepository installmentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final BigDecimal MIN_PAYMENT = new BigDecimal("1000");

    @Transactional
    public PaymentResponseDto processPayment(String studentId, BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.compareTo(MIN_PAYMENT) < 0) {
            throw new ContractException("Minimum payment amount is 1,000 RWF");
        }

        // Record payment locally against the student's active contract installments.
        // No external bank call — payment confirmation is handled by the portal.

        List<Contract> activeContracts = contractRepository.findByStudentIdAndStatus(
            studentId, Contract.ContractStatus.ACTIVE);
        
        if (activeContracts.isEmpty()) {
            log.warn("No ACTIVE contract found for student {}", studentId);
            return null;
        }

        Contract activeContract = activeContracts.get(0);
        log.info("Processing payment of {} for student {} on contract {}", paymentAmount, studentId, activeContract.getId());

        List<ContractInstallment> unpaidInstallments = installmentRepository
            .findByContractIdAndStatusInPENDING_OR_PARTIALLY_PAID(activeContract.getId());
        
        log.info("Found {} unpaid installments for contract {}", unpaidInstallments.size(), activeContract.getId());

        BigDecimal remainingPayment = paymentAmount;
        BigDecimal totalPaid = BigDecimal.ZERO;
        List<PaymentResponseDto.InstallmentUpdateDto> installmentUpdates = new ArrayList<>();

        for (ContractInstallment installment : unpaidInstallments) {
            if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal currentlyPaid = installment.getAmountPaid() != null ? installment.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal amountNeededToClear = installment.getAmountDue().subtract(currentlyPaid);
            BigDecimal amountApplied = amountNeededToClear.min(remainingPayment);

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
            totalPaid = totalPaid.add(amountApplied);

            installmentUpdates.add(new PaymentResponseDto.InstallmentUpdateDto(
                installment.getId(),
                installment.getInstallmentNumber(),
                amountApplied,
                installment.getStatus().name(),
                installment.getStatus() == ContractInstallment.InstallmentStatus.PAID
            ));
        }

        List<ContractInstallment> remainingUnpaid = installmentRepository
            .findByContractIdAndStatusNotOrderByDeadlineDateAsc(activeContract.getId(), ContractInstallment.InstallmentStatus.PAID);

        if (remainingUnpaid.isEmpty()) {
            activeContract.setStatus(Contract.ContractStatus.COMPLETED);
            contractRepository.save(activeContract);
        }

        // Send websocket notifications
        if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            NotificationMessage studentMsg = NotificationMessage.builder()
                .title("Payment Received")
                .message("We have successfully received your payment of " + totalPaid + " RWF.")
                .type("INFO")
                .contractId(activeContract.getId())
                .studentId(studentId)
                .timestamp(LocalDateTime.now())
                .build();
            messagingTemplate.convertAndSend("/topic/notifications/" + studentId, studentMsg);
            
            NotificationMessage staffMsg = NotificationMessage.builder()
                .title("New Payment")
                .message("Student " + studentId + " paid " + totalPaid + " RWF for contract " + activeContract.getId())
                .type("INFO")
                .contractId(activeContract.getId())
                .studentId(studentId)
                .timestamp(LocalDateTime.now())
                .build();
            messagingTemplate.convertAndSend("/topic/staff/notifications", staffMsg);
        }

        return PaymentResponseDto.builder()
            .contractId(activeContract.getId())
            .totalAmountPaid(totalPaid)
            .installmentUpdates(installmentUpdates)
            .build();
    }
}