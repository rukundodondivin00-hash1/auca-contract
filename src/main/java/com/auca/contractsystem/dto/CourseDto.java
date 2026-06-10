package com.auca.contractsystem.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDto {
    private String code;
    private String name;
    private Integer credits;
    private String day;
    private String startTime;
    private String endTime;
    private String room;
}