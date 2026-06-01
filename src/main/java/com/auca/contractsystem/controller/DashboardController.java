package com.auca.contractsystem.controller;

import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Main dashboard — returns all student financial data in one call")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get full student dashboard — fetches AUCA data + our contract data")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(Authentication auth) {
        String studentId = auth.getName();
        DashboardResponse dashboard = dashboardService.getDashboard(studentId);
        return ResponseEntity.ok(ApiResponse.success("Dashboard loaded", dashboard));
    }

    @GetMapping("/admin/contracts")
    @Operation(summary = "Admin — get all contracts")
    public ResponseEntity<ApiResponse<List<ContractDto>>> getAllContracts() {
        List<ContractDto> contracts = dashboardService.getAllContracts();
        return ResponseEntity.ok(ApiResponse.success("Contracts retrieved", contracts));
    }
}
