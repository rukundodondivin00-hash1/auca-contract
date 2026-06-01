package com.auca.contractsystem.controller;

import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "Contract management")
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    @Operation(summary = "Create a new contract with installments")
    public ResponseEntity<ApiResponse<ContractDto>> createContract(
            Authentication auth,
            @Valid @RequestBody ContractRequest request) {
        String studentId = auth.getName();
        ContractDto contract = contractService.createContract(studentId, request);
        return ResponseEntity.ok(ApiResponse.success("Contract created successfully", contract));
    }

    @GetMapping("/my-contracts")
    @Operation(summary = "Get all contracts for the authenticated student")
    public ResponseEntity<ApiResponse<List<ContractDto>>> getMyContracts(Authentication auth) {
        String studentId = auth.getName();
        List<ContractDto> contracts = contractService.getStudentContracts(studentId);
        return ResponseEntity.ok(ApiResponse.success("Contracts retrieved", contracts));
    }
}
