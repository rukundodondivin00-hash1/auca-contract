package com.auca.contractsystem.dto.staff;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StaffGrantPermitRequest {
    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Permit Type is required")
    private String permitType;

    @NotBlank(message = "Reason is required")
    private String reason;
}
