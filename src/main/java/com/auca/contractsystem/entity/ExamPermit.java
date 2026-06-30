package com.auca.contractsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_permits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamPermit {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    private String id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "term_id", nullable = false)
    private String termId;

    @Column(name = "permit_type", nullable = false)
    private String permitType;

    @Column(name = "granted_by")
    private String grantedBy;

    @Column(name = "grant_reason", columnDefinition = "TEXT")
    private String grantReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
