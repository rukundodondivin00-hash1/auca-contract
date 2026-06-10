package com.auca.contractsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AucaStudentDashboardResponse {
    private BigDecimal cumulativeGPA;
    private Integer totalCreditsEarned;
    private RegistrationSummary registrationSummary;

    @Data
    public static class RegistrationSummary {
        private String currentTermId;
        private Integer registeredCredits;
        private List<CourseItem> courses;
    }

    @Data
    public static class CourseItem {
        private String code;
        private String name;
        private Integer credits;
    }
}