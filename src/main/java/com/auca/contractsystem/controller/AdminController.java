package com.auca.contractsystem.controller;

import com.auca.contractsystem.dto.admin.AdminContractDto;
import com.auca.contractsystem.dto.admin.AdminInstallmentDto;
import com.auca.contractsystem.dto.admin.AdminPenaltyDto;
import com.auca.contractsystem.dto.admin.AdminStudentSummaryDto;
import com.auca.contractsystem.entity.Contract;
import com.auca.contractsystem.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin read-only endpoints")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/contracts")
    @Operation(summary = "Get all contracts with pagination")
    public ResponseEntity<?> getAllContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(adminService.getAllContracts(page, size, sortBy, direction));
    }

    @GetMapping("/contracts/{id}")
    @Operation(summary = "Get contract by ID")
    public ResponseEntity<?> getContract(@PathVariable String id) {
        AdminContractDto contract = adminService.getContractById(id);
        return ResponseEntity.ok(contract);
    }

    @GetMapping("/contracts/student/{studentId}")
    @Operation(summary = "Get contracts by student ID")
    public ResponseEntity<?> getContractsByStudent(@PathVariable String studentId) {
        List<AdminContractDto> contracts = adminService.getContractsByStudent(studentId);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/contracts/status/{status}")
    @Operation(summary = "Get contracts by status")
    public ResponseEntity<?> getContractsByStatus(@PathVariable Contract.ContractStatus status) {
        List<AdminContractDto> contracts = adminService.getContractsByStatus(status);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/installments")
    @Operation(summary = "Get all installments with pagination")
    public ResponseEntity<?> getAllInstallments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(adminService.getAllInstallments(page, size, sortBy, direction));
    }

    @GetMapping("/installments/contract/{contractId}")
    @Operation(summary = "Get installments by contract")
    public ResponseEntity<?> getInstallmentsByContract(@PathVariable String contractId) {
        List<AdminInstallmentDto> installments = adminService.getInstallmentsByContract(contractId);
        return ResponseEntity.ok(installments);
    }

    @GetMapping("/penalties")
    @Operation(summary = "Get all penalty history with pagination")
    public ResponseEntity<?> getAllPenaltyHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(adminService.getAllPenaltyHistory(page, size, sortBy, direction));
    }

    @GetMapping("/penalties/installment/{installmentId}")
    @Operation(summary = "Get penalty history by installment")
    public ResponseEntity<?> getPenaltyHistoryByInstallment(@PathVariable String installmentId) {
        List<AdminPenaltyDto> penalties = adminService.getPenaltyHistoryByInstallment(installmentId);
        return ResponseEntity.ok(penalties);
    }

    @GetMapping("/penalties/contract/{contractId}")
    @Operation(summary = "Get penalty history by contract")
    public ResponseEntity<?> getPenaltyHistoryByContract(@PathVariable String contractId) {
        List<AdminPenaltyDto> penalties = adminService.getPenaltyHistoryByContract(contractId);
        return ResponseEntity.ok(penalties);
    }

    @GetMapping("/students")
    @Operation(summary = "Search students with pagination")
    public ResponseEntity<?> searchStudents(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.searchStudents(page, size, keyword));
    }

    @GetMapping("/students/{studentId}/summary")
    @Operation(summary = "Get student financial summary")
    public ResponseEntity<?> getStudentSummary(@PathVariable String studentId) {
        AdminStudentSummaryDto summary = adminService.getStudentSummary(studentId);
        return ResponseEntity.ok(summary);
    }
}
