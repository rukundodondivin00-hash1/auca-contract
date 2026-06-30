package com.auca.contractsystem.controller;

import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.service.ContractService;
import com.auca.contractsystem.service.TermConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "Contract management")
public class ContractController {

    private final ContractService contractService;
    private final TermConfigService termConfigService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    @Operation(summary = "Create a new contract with installments")
    public ResponseEntity<ApiResponse<ContractDto>> createContract(
            Authentication auth,
            @Valid @RequestBody ContractRequest request) {
        String studentId = auth.getName();
        ContractDto contract = contractService.createContract(studentId, request);

        // Notify staff that a new contract has been signed
        NotificationMessage staffMsg = NotificationMessage.builder()
            .title("New Contract Signed")
            .message("Student " + studentId + " has signed a new payment contract (ID: " + contract.getId() + ").")
            .type("INFO")
            .contractId(contract.getId())
            .studentId(studentId)
            .timestamp(LocalDateTime.now())
            .build();
        messagingTemplate.convertAndSend("/topic/staff/notifications", staffMsg);

        return ResponseEntity.ok(ApiResponse.success("Contract created successfully", contract));
    }

    @GetMapping("/my-contracts")
    @Operation(summary = "Get all contracts for the authenticated student")
    public ResponseEntity<ApiResponse<List<ContractDto>>> getMyContracts(Authentication auth) {
        String studentId = auth.getName();
        List<ContractDto> contracts = contractService.getStudentContracts(studentId);
        return ResponseEntity.ok(ApiResponse.success("Contracts retrieved", contracts));
    }

    @GetMapping("/my-permits")
    @Operation(summary = "Get all exam permits for the authenticated student")
    public ResponseEntity<ApiResponse<List<ExamPermitDto>>> getMyPermits(Authentication auth) {
        String studentId = auth.getName();
        List<ExamPermitDto> permits = contractService.getStudentPermits(studentId);
        return ResponseEntity.ok(ApiResponse.success("Permits retrieved", permits));
    }

    @GetMapping("/my-penalties")
    @Operation(summary = "Get all penalty history for the authenticated student")
    public ResponseEntity<ApiResponse<List<com.auca.contractsystem.dto.staff.StaffPenaltyDto>>> getMyPenalties(Authentication auth) {
        String studentId = auth.getName();
        List<com.auca.contractsystem.dto.staff.StaffPenaltyDto> penalties = contractService.getStudentPenalties(studentId);
        return ResponseEntity.ok(ApiResponse.success("Penalties retrieved", penalties));
    }

    @GetMapping("/term-config")
    @Operation(summary = "Get all term configurations")
    public ResponseEntity<List<TermConfigDto>> getAllTermConfigs() {
        return ResponseEntity.ok(termConfigService.getAllConfigs());
    }

    @GetMapping("/term-config/{termId}")
    @Operation(summary = "Get configuration for a specific term")
    public ResponseEntity<TermConfigDto> getTermConfig(@PathVariable String termId) {
        return termConfigService.getConfigByTermId(termId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
