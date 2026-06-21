package com.auca.contractsystem.controller;

import com.auca.contractsystem.dto.ApiResponse;
import com.auca.contractsystem.dto.PaymentRequestDto;
import com.auca.contractsystem.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Student payment processing")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    @Operation(summary = "Confirm payment and update installment status")
    public ResponseEntity<ApiResponse<String>> confirmPayment(
            @RequestHeader("X-Student-Id") String studentId,
            @RequestBody PaymentRequestDto request) {
        log.info("Payment request received - StudentId: {}, Amount: {}", studentId, request.getAmount());
        paymentService.processPayment(studentId, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", "Payment confirmed"));
    }
}