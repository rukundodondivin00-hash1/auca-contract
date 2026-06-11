package com.auca.contractsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AucaStudentDashboardResponse {
    private GpaData gpaData;
    private List<RecentGrade> recentGrades;
    private Bulletin bulletin;
    private List<Announcement> announcements;
    private RegistrationSummary registrationSummary;
    private Integer totalPassedCourses;

    @Data
    public static class GpaData {
        private BigDecimal cumulativeGPA;
        private BigDecimal currentSemesterGPA;
        private Integer totalCreditsEarned;
        private Integer totalCreditsAttempted;
        private Integer currentSemesterCredits;
        private List<SemesterGpa> semesterGPAs;
    }

    @Data
    public static class SemesterGpa {
        private String termId;
        private BigDecimal semesterGPA;
        private Integer creditsEarned;
        private Integer creditsAttempted;
    }

    @Data
    public static class RecentGrade {
        private String courseCode;
        private String courseName;
        private Integer credits;
        private BigDecimal grade;
        private BigDecimal score;
        private String status;
        private String termId;
        private Integer year;
        private String semester;
    }

    @Data
    public static class Bulletin {
        private Integer id;
        private String department;
        private String departmentCode;
        private String facultyName;
        private String programName;
        private List<CourseDetail> courseDetails;
    }

    @Data
    public static class CourseDetail {
        private String code;
        private String name;
        private Integer credits;
        private Integer expectedTerm;
        private Boolean generalCourse;
    }

    @Data
    public static class Announcement {
        private Integer id;
        private String postedBy;
        private String title;
        private String type;
        private String content;
        private String destination;
        private String date;
    }

    @Data
    public static class RegistrationSummary {
        private String currentTermId;
        private Integer registeredCredits;
        private Integer courseCount;
        private String registrationStatus;
        private List<CourseItem> courses;
    }

    @Data
    public static class CourseItem {
        private Integer id;
        private String courseName;
        private Integer credits;
        private String courseCode;
        private String group;
        private String lecturerName;
        private Integer size;
        private String roomName;
        private String day;
        private String startTime;
        private String endTime;
        private Boolean locked;
    }
}
