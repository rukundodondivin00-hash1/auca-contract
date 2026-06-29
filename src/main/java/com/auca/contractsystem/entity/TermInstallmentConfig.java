package com.auca.contractsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "term_installment_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermInstallmentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentage;

    @Column(name = "deadline_date", nullable = false)
    private LocalDate deadlineDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_config_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TermConfig termConfig;
}
