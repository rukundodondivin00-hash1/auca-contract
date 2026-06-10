package com.auca.contractsystem.dto;

import lombok.Data;
import java.util.List;

@Data
public class AucaTranscriptResponse {
    private List<AcademicYearDto> academicYears;

    @Data
    public static class AcademicYearDto {
        private String year;
        private String academicYear;
        private List<SemesterDto> semesters;
    }

    @Data
    public static class SemesterDto {
        private String semester;
        private String term;
        private List<CourseGradeDto> courses;
    }

    @Data
    public static class CourseGradeDto {
        private String code;
        private String name;
        private String credits;
        private String grade;
    }
}