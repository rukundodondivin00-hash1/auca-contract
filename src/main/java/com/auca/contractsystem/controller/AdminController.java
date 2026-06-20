package com.auca.contractsystem.controller;

import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.dto.admin.*;
import com.auca.contractsystem.entity.Contract;
import com.auca.contractsystem.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin read-only endpoints")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    @Operation(summary = "Admin login with credentials")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = adminService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("Admin login successful", response));
    }

    @GetMapping("/contracts")
    @Operation(summary = "Get all contracts with pagination")
    public ResponseEntity<PaginatedResponse<AdminContractDto>> getAllContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Page<AdminContractDto> contractPage = adminService.getAllContracts(page, size, sortBy, direction);
        PaginatedResponse<AdminContractDto> response = PaginatedResponse.<AdminContractDto>builder()
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
    public ResponseEntity<AdminContractDto> getContract(@PathVariable String id) {
        AdminContractDto contract = adminService.getContractById(id);
        return ResponseEntity.ok(contract);
    }

    @GetMapping("/contracts/student/{studentId}")
    @Operation(summary = "Get contracts by student ID")
    public ResponseEntity<PaginatedResponse<AdminContractDto>> getContractsByStudent(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminContractDto> contractPage = adminService.getContractsByStudentPaginated(studentId, page, size);
        PaginatedResponse<AdminContractDto> response = PaginatedResponse.<AdminContractDto>builder()
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
    public ResponseEntity<PaginatedResponse<AdminContractDto>> getContractsByStatus(
            @PathVariable Contract.ContractStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Page<AdminContractDto> contractPage = adminService.getContractsByStatusPaginated(status, page, size, sortBy, direction);
        PaginatedResponse<AdminContractDto> response = PaginatedResponse.<AdminContractDto>builder()
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
    public ResponseEntity<PaginatedResponse<AdminInstallmentDto>> getAllInstallments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Page<AdminInstallmentDto> installmentPage = adminService.getAllInstallments(page, size, sortBy, direction);
        PaginatedResponse<AdminInstallmentDto> response = PaginatedResponse.<AdminInstallmentDto>builder()
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
    public ResponseEntity<List<AdminInstallmentDto>> getInstallmentsByContract(@PathVariable String contractId) {
        List<AdminInstallmentDto> installments = adminService.getInstallmentsByContract(contractId);
        return ResponseEntity.ok(installments);
    }

    @GetMapping("/penalties")
    @Operation(summary = "Get all penalty history with pagination")
    public ResponseEntity<PaginatedResponse<AdminPenaltyDto>> getAllPenaltyHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Page<AdminPenaltyDto> penaltyPage = adminService.getAllPenaltyHistory(page, size, sortBy, direction);
        PaginatedResponse<AdminPenaltyDto> response = PaginatedResponse.<AdminPenaltyDto>builder()
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
    public ResponseEntity<List<AdminPenaltyDto>> getPenaltyHistoryByInstallment(@PathVariable String installmentId) {
        List<AdminPenaltyDto> penalties = adminService.getPenaltyHistoryByInstallment(installmentId);
        return ResponseEntity.ok(penalties);
    }

    @GetMapping("/penalties/contract/{contractId}")
    @Operation(summary = "Get penalty history by contract")
    public ResponseEntity<List<AdminPenaltyDto>> getPenaltyHistoryByContract(@PathVariable String contractId) {
        List<AdminPenaltyDto> penalties = adminService.getPenaltyHistoryByContract(contractId);
        return ResponseEntity.ok(penalties);
    }

    @GetMapping("/students")
    @Operation(summary = "Search students with pagination")
    public ResponseEntity<PaginatedResponse<AdminStudentSummaryDto>> searchStudents(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminStudentSummaryDto> studentPage = adminService.searchStudents(page, size, keyword);
        PaginatedResponse<AdminStudentSummaryDto> response = PaginatedResponse.<AdminStudentSummaryDto>builder()
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
    public ResponseEntity<AdminStudentSummaryDto> getStudentSummary(@PathVariable String studentId) {
        AdminStudentSummaryDto summary = adminService.getStudentSummary(studentId);
        return ResponseEntity.ok(summary);
    }
}
