package com.auca.contractsystem.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminInstallmentDto {
    
    private String id;
    private String contractId;
    private Integer installmentNumber;
    
    // Updated to match the React frontend's 'dueDate' expectation
    @JsonProperty("dueDate")
    private LocalDate deadlineDate;
    
    // Updated to match the React frontend's 'amount' expectation
    @JsonProperty("amount")
    private BigDecimal amountDue;
    
    @JsonProperty("amountPaid")
    private BigDecimal amountPaid;
    
    @JsonProperty("penaltyAmount")
    private BigDecimal penaltyAmount;
    
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String studentId;
    private String studentName;
    private String termId;
}