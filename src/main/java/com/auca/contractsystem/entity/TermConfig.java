package com.auca.contractsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "term_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "term_id", unique = true, nullable = false)
    private String termId;

    @OneToMany(mappedBy = "termConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TermInstallmentConfig> installments = new ArrayList<>();

    @Column(name = "penalty_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal penaltyPercentage;

    @Column(name = "initial_payment_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal initialPaymentPercentage = new BigDecimal("100.00");

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
