package com.auca.contractsystem.controller;

import com.auca.contractsystem.dto.ApiResponse;
import com.auca.contractsystem.dto.PaymentRequestDto;
import com.auca.contractsystem.dto.PaymentResponseDto;
import com.auca.contractsystem.entity.PrePayment;
import com.auca.contractsystem.repository.PrePaymentRepository;
import com.auca.contractsystem.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Student payment processing")
public class PaymentController {

    private final PaymentService paymentService;
    private final PrePaymentRepository prePaymentRepository;

    /**
     * Pre-contract payment: student pays an amount (toward the 50% minimum).
     * This is recorded in our DB and used for contract eligibility checks.
     * POST /api/payments/pre-payment
     */
    @PostMapping("/pre-payment")
    @Operation(summary = "Record a pre-contract payment (toward 50% deposit)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recordPrePayment(
            Authentication auth,
            @RequestBody Map<String, Object> body) {

        String studentId = auth.getName();
        BigDecimal amount = new BigDecimal(body.getOrDefault("amount", "0").toString());
        String termId   = body.getOrDefault("termId", "").toString();
        String channel  = body.getOrDefault("channel", "MOMO").toString();
        String feeType  = body.getOrDefault("feeType", "TUITION_FEE").toString();
        String phone    = body.getOrDefault("phoneNumber", "").toString();

        if (amount.compareTo(BigDecimal.valueOf(1000)) < 0) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Minimum payment is 1,000 RWF"));
        }

        PrePayment payment = PrePayment.builder()
            .studentId(studentId)
            .termId(termId)
            .amount(amount)
            .channel(channel)
            .feeType(feeType)
            .phoneNumber(phone)
            .build();
        prePaymentRepository.save(payment);

        BigDecimal totalPaid = prePaymentRepository.sumAmountByStudentId(studentId);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Payment of " + amount + " RWF recorded successfully.");
        result.put("amountPaid", amount);
        result.put("totalPaidToDate", totalPaid);
        result.put("studentId", studentId);

        log.info("Pre-payment recorded: studentId={}, amount={}, totalPaid={}", studentId, amount, totalPaid);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded", result));
    }

    /**
     * Get total pre-payments for student (used to check 50% eligibility).
     * GET /api/payments/my-balance
     */
    @GetMapping("/my-balance")
    @Operation(summary = "Get student's total pre-payments and eligibility status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyBalance(Authentication auth) {
        String studentId = auth.getName();
        BigDecimal totalPaid = prePaymentRepository.sumAmountByStudentId(studentId);

        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("totalPaid", totalPaid);
        result.put("payments", prePaymentRepository.findByStudentId(studentId));
        return ResponseEntity.ok(ApiResponse.success("Balance retrieved", result));
    }

    /**
     * Post-contract payment: distributes payment across active contract installments.
     * POST /api/payments/confirm
     */
    @PostMapping("/confirm")
    @Operation(summary = "Confirm post-contract payment and update installment status")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> confirmPayment(
            Authentication auth,
            @RequestBody PaymentRequestDto request) {

        String studentId = auth.getName();
        log.info("Post-contract payment request - StudentId: {}, Amount: {}", studentId, request.getAmount());
        PaymentResponseDto response = paymentService.processPayment(studentId, request.getAmount());

        if (response == null) {
            // No active contract — record as pre-payment instead
            PrePayment payment = PrePayment.builder()
                .studentId(studentId)
                .amount(request.getAmount())
                .channel("DIRECT")
                .feeType("TUITION_FEE")
                .build();
            prePaymentRepository.save(payment);
            BigDecimal totalPaid = prePaymentRepository.sumAmountByStudentId(studentId);
            return ResponseEntity.ok(ApiResponse.success(
                "Payment of " + request.getAmount() + " RWF recorded. Total paid: " + totalPaid + " RWF.", null));
        }

        // Record the amount successfully applied to installments into PrePayment history table
        BigDecimal amountApplied = response.getTotalAmountPaid();
        if (amountApplied != null && amountApplied.compareTo(BigDecimal.ZERO) > 0) {
            PrePayment installmentRecord = PrePayment.builder()
                .studentId(studentId)
                .amount(amountApplied)
                .channel(request.getChannel() != null ? request.getChannel() : "DIRECT")
                .feeType("INSTALLMENT_PAYMENT")
                .build();
            prePaymentRepository.save(installmentRecord);
        }

        // If the contract was fully paid but there was extra money (overpayment), save it as PrePayment
        if (request.getAmount().compareTo(amountApplied) > 0) {
            BigDecimal overpayment = request.getAmount().subtract(amountApplied);
            PrePayment overpaymentRecord = PrePayment.builder()
                .studentId(studentId)
                .amount(overpayment)
                .channel("DIRECT")
                .feeType("OVERPAYMENT")
                .build();
            prePaymentRepository.save(overpaymentRecord);
            log.info("Student {} overpaid contract by {}. Saved as PrePayment.", studentId, overpayment);
        }

        String message = buildPaymentMessage(response, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    private String buildPaymentMessage(PaymentResponseDto response, BigDecimal totalAmount) {
        if (response.getInstallmentUpdates() == null || response.getInstallmentUpdates().isEmpty()) {
            return "Payment of " + totalAmount + " RWF received. No installments to update.";
        }
        StringBuilder sb = new StringBuilder("Payment of ").append(totalAmount).append(" RWF received. ");
        for (PaymentResponseDto.InstallmentUpdateDto update : response.getInstallmentUpdates()) {
            sb.append("Installment ").append(update.getInstallmentNumber());
            if (update.getFullyPaid()) {
                sb.append(" fully paid.");
            } else {
                sb.append(" partially paid (").append(update.getAmountPaid()).append(" RWF).");
            }
            sb.append(" ");
        }
        return sb.toString();
    }
}