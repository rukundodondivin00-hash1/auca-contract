package com.auca.contractsystem.dto;

import lombok.Data;

@Data
public class AucaTermResponse {
    private String id;
    private String year;
    private String semester;
    private String description;
    private String startDate;
    private String endDate;
    private Boolean active;
}
