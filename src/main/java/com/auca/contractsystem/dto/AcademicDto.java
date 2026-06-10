package com.auca.contractsystem.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcademicDto {
    private String activeTerm;
    private Integer semesterNumber;
    private String semesterName;
    private Integer credits;
    private BigDecimal cumulativeGpa;
    private Integer registeredCredits;
}