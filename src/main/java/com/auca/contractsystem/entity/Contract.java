package com.auca.contractsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "term_id")
    private String termId;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "semester")
    private String semester;

    @Column(name = "total_fees", precision = 15, scale = 2)
    private BigDecimal totalFees;

    @Column(name = "balance_at_signing", precision = 15, scale = 2)
    private BigDecimal balanceAtSigning;

    @Column(name = "amount_paid_at_signing", precision = 15, scale = 2)
    private BigDecimal amountPaidAtSigning;

    @Column(name = "remaining_at_signing", precision = 15, scale = 2)
    private BigDecimal remainingAtSigning;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    @Column(name = "agreed", nullable = false)
    private Boolean agreed = false;

    @Column(name = "agreed_date")
    private LocalDate agreedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContractInstallment> installments;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = ContractStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ContractStatus {
        PENDING, ACTIVE, COMPLETED, CANCELLED, OVERDUE
    }
}
