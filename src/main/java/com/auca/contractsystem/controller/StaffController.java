package com.auca.contractsystem.controller;

import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.dto.staff.*;
import com.auca.contractsystem.entity.Contract;
import com.auca.contractsystem.service.StaffService;
import com.auca.contractsystem.service.TermConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Staff", description = "Endpoints for staff members")
public class StaffController {

    private final StaffService staffService;
    private final TermConfigService termConfigService;

    @PostMapping("/login")
    @Operation(summary = "Staff login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(staffService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/signup")
    @Operation(summary = "Staff signup")
    public ResponseEntity<ApiResponse<LoginResponse>> signup(@Valid @RequestBody StaffSignupRequest request) {
        LoginResponse response = staffService.signup(request);
        return ResponseEntity.ok(ApiResponse.success("Staff signup successful", response));
    }

    @GetMapping("/contracts")
    @Operation(summary = "Get all contracts with pagination")
    public ResponseEntity<PaginatedResponse<StaffContractDto>> getAllContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Page<StaffContractDto> contractPage = staffService.getAllContracts(page, size, sortBy, direction);
        PaginatedResponse<StaffContractDto> response = PaginatedResponse.<StaffContractDto>builder()
            .content(contractPage.getContent())
            .totalElements(contractPage.getTotalElements())
            .totalPages(contractPage.getTotalPages())
            .number(contractPage.getNumber())
            .size(contractPage.getSize())
            .first(contractPage.isFirst())
            .last(contractPage.isLast())
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contracts/{id}")
    @Operation(summary = "Get contract by ID")
    public ResponseEntity<StaffContractDto> getContract(@PathVariable String id) {
        StaffContractDto contract = staffService.getContractById(id);
        return ResponseEntity.ok(contract);
    }

    @PostMapping("/contracts/grant-permit")
    @Operation(summary = "Grant an exam permit directly")
    public ResponseEntity<StaffExamPermitDto> grantPermit(
            org.springframework.security.core.Authentication auth,
            @Valid @RequestBody StaffGrantPermitRequest request) {
        StaffExamPermitDto permit = staffService.grantPermit(auth.getName(), request);
        return ResponseEntity.ok(permit);
    }
    
    @GetMapping("/permits")
    @Operation(summary = "Get all granted exam permits")
    public ResponseEntity<PaginatedResponse<StaffExamPermitDto>> getAllPermits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Page<StaffExamPermitDto> permitPage = staffService.getAllPermits(page, size, sortBy, direction);
        PaginatedResponse<StaffExamPermitDto> response = PaginatedResponse.<StaffExamPermitDto>builder()
            .content(permitPage.getContent())
            .totalElements(permitPage.getTotalElements())
            .totalPages(permitPage.getTotalPages())
            .number(permitPage.getNumber())
            .size(permitPage.getSize())
            .first(permitPage.isFirst())
            .last(permitPage.isLast())
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contracts/student/{studentId}")
    @Operation(summary = "Get contracts by student ID")
    public ResponseEntity<PaginatedResponse<StaffContractDto>> getContractsByStudent(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<StaffContractDto> contractPage = staffService.getContractsByStudentPaginated(studentId, page, size);
        PaginatedResponse<StaffContractDto> response = PaginatedResponse.<StaffContractDto>builder()
            .content(contractPage.getContent())
            .totalElements(contractPage.getTotalElements())
            .totalPages(contractPage.getTotalPages())
            .number(contractPage.getNumber())
            .size(contractPage.getSize())
            .first(contractPage.isFirst())
            .last(contractPage.isLast())
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contracts/status/{status}")
    @Operation(summary = "Get contracts by status")
    public ResponseEntity<PaginatedResponse<StaffContractDto>> getContractsByStatus(
            @PathVariable Contract.ContractStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Page<StaffContractDto> contractPage = staffService.getContractsByStatusPaginated(status, page, size, sortBy, direction);
        PaginatedResponse<StaffContractDto> response = PaginatedResponse.<StaffContractDto>builder()
            .content(contractPage.getContent())
            .totalElements(contractPage.getTotalElements())
            .totalPages(contractPage.getTotalPages())
            .number(contractPage.getNumber())
            .size(contractPage.getSize())
            .first(contractPage.isFirst())
            .last(contractPage.isLast())
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/installments")
    @Operation(summary = "Get all installments with pagination")
    public ResponseEntity<PaginatedResponse<StaffInstallmentDto>> getAllInstallments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Page<StaffInstallmentDto> installmentPage = staffService.getAllInstallments(page, size, sortBy, direction);
        PaginatedResponse<StaffInstallmentDto> response = PaginatedResponse.<StaffInstallmentDto>builder()
            .content(installmentPage.getContent())
            .totalElements(installmentPage.getTotalElements())
            .totalPages(installmentPage.getTotalPages())
            .number(installmentPage.getNumber())
            .size(installmentPage.getSize())
            .first(installmentPage.isFirst())
            .last(installmentPage.isLast())
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/installments/contract/{contractId}")
    @Operation(summary = "Get installments by contract")
    public ResponseEntity<List<StaffInstallmentDto>> getInstallmentsByContract(@PathVariable String contractId) {
        List<StaffInstallmentDto> installments = staffService.getInstallmentsByContract(contractId);
        return ResponseEntity.ok(installments);
    }

    @GetMapping("/penalties")
    @Operation(summary = "Get all penalty history with pagination")
    public ResponseEntity<PaginatedResponse<StaffPenaltyDto>> getAllPenaltyHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Page<StaffPenaltyDto> penaltyPage = staffService.getAllPenaltyHistory(page, size, sortBy, direction);
        PaginatedResponse<StaffPenaltyDto> response = PaginatedResponse.<StaffPenaltyDto>builder()
            .content(penaltyPage.getContent())
            .totalElements(penaltyPage.getTotalElements())
            .totalPages(penaltyPage.getTotalPages())
            .number(penaltyPage.getNumber())
            .size(penaltyPage.getSize())
            .first(penaltyPage.isFirst())
            .last(penaltyPage.isLast())
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/penalties/installment/{installmentId}")
    @Operation(summary = "Get penalty history by installment")
    public ResponseEntity<List<StaffPenaltyDto>> getPenaltyHistoryByInstallment(@PathVariable String installmentId) {
        List<StaffPenaltyDto> penalties = staffService.getPenaltyHistoryByInstallment(installmentId);
        return ResponseEntity.ok(penalties);
    }

    @GetMapping("/penalties/contract/{contractId}")
    @Operation(summary = "Get penalty history by contract")
    public ResponseEntity<List<StaffPenaltyDto>> getPenaltyHistoryByContract(@PathVariable String contractId) {
        List<StaffPenaltyDto> penalties = staffService.getPenaltyHistoryByContract(contractId);
        return ResponseEntity.ok(penalties);
    }

    @GetMapping("/students")
    @Operation(summary = "Search students with pagination")
    public ResponseEntity<PaginatedResponse<StaffStudentSummaryDto>> searchStudents(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<StaffStudentSummaryDto> studentPage = staffService.searchStudents(page, size, keyword);
        PaginatedResponse<StaffStudentSummaryDto> response = PaginatedResponse.<StaffStudentSummaryDto>builder()
            .content(studentPage.getContent())
            .totalElements(studentPage.getTotalElements())
            .totalPages(studentPage.getTotalPages())
            .number(studentPage.getNumber())
            .size(studentPage.getSize())
            .first(studentPage.isFirst())
            .last(studentPage.isLast())
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/students/{studentId}/summary")
    @Operation(summary = "Get student financial summary")
    public ResponseEntity<StaffStudentSummaryDto> getStudentSummary(@PathVariable String studentId) {
        StaffStudentSummaryDto summary = staffService.getStudentSummary(studentId);
        return ResponseEntity.ok(summary);
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

    @PostMapping("/term-config")
    @Operation(summary = "Create or update a term configuration")
    public ResponseEntity<TermConfigDto> saveTermConfig(@Valid @RequestBody TermConfigDto request) {
        return ResponseEntity.ok(termConfigService.saveOrUpdateConfig(request));
    }
}
