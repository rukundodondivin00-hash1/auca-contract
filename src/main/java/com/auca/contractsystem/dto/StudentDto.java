package com.auca.contractsystem.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentDto {
    private String id;
    private String name;
    private String email;
    private String department;
    private String departmentCode;
    private String program;
}