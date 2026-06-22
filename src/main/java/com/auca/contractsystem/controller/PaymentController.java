package com.auca.contractsystem.controller;

import com.auca.contractsystem.dto.ApiResponse;
import com.auca.contractsystem.dto.PaymentRequestDto;
import com.auca.contractsystem.dto.PaymentResponseDto;
import com.auca.contractsystem.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Student payment processing")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    @Operation(summary = "Confirm payment and update installment status")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> confirmPayment(
            @RequestHeader("X-Student-Id") String studentId,
            @RequestBody PaymentRequestDto request) {
        log.info("Payment request received - StudentId: {}, Amount: {}", studentId, request.getAmount());
        PaymentResponseDto response = paymentService.processPayment(studentId, request.getAmount());
        
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.success(
                "No active contract found for student", 
                null));
        }
        
        String message = buildPaymentMessage(response, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
    
    private String buildPaymentMessage(PaymentResponseDto response, BigDecimal totalAmount) {
        if (response.getInstallmentUpdates() == null || response.getInstallmentUpdates().isEmpty()) {
            return "Payment of " + totalAmount + " RWF received. No installments to update.";
        }
        
        StringBuilder sb = new StringBuilder("Payment of ");
        sb.append(totalAmount).append(" RWF received. ");
        
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