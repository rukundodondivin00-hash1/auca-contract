package com.auca.contractsystem.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AucaRegistrationResponse {
    private String studentName;
    private String studentDepartment;
    private String studentFaculty;
    private String studentProgram;
    private BigDecimal totalFee;
    private String status;
}
