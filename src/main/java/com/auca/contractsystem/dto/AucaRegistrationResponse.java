package com.auca.contractsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AucaRegistrationResponse {
    private String studentName;
    private String studentDepartment;
    private String studentDepartmentCode;
    private String studentFaculty;
    private String studentProgram;
    private BigDecimal totalFee;
    private String status;
    private Boolean isRegistrationOpen;
    private List<CourseItem> courses;

    @Data
    public static class CourseItem {
        private String code;
        private String name;
        private Integer credits;
        private String day;
        private String startTime;
        private String endTime;
        private String room;
    }
}
