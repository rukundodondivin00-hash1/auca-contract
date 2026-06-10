package com.auca.contractsystem.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnifiedDashboardResponse {
    private StudentDto student;
    private AcademicDto academic;
    private FinancialDto financial;
    private ContractResponseDto contract;
    private RegistrationDto registration;
}