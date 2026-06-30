package com.auca.contractsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamPermitDto {
    private String id;
    private String studentId;
    private String studentName;
    private String termId;
    private String permitType;
    private String grantedBy;
    private String grantReason;
    private LocalDateTime createdAt;
}
