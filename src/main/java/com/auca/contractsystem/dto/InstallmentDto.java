package com.auca.contractsystem.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentDto {
    private String id;
    private String contractId;
    private Integer installmentNumber;
    private LocalDate deadlineDate;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private BigDecimal penaltyAmount;
    private String status;
    private LocalDateTime paidAt;
    private List<PenaltyHistoryDto> penaltyHistories;
}
