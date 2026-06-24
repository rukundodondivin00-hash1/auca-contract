package com.auca.contractsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks payments made by a student BEFORE a contract is signed.
 * The sum of all PrePayments for a student/term determines if they have
 * paid ≥ 50% of their totalFee (making them eligible to sign a contract).
 */
@Entity
@Table(name = "pre_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "term_id")
    private String termId;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "channel")
    private String channel;

    @Column(name = "fee_type")
    private String feeType;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
