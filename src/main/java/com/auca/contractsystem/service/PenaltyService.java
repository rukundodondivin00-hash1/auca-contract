package com.auca.contractsystem.service;

import com.auca.contractsystem.entity.*;
import com.auca.contractsystem.repository.*;
import com.auca.contractsystem.util.PenaltyCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PenaltyService {

    private final InstallmentRepository installmentRepository;
    private final PenaltyRepository penaltyRepository;
    private final ContractRepository contractRepository;
    private final PenaltyCalculator penaltyCalculator;

    @Transactional
    public void checkAndApplyPenalties() {
        log.info("Running penalty check...");
        LocalDate today = LocalDate.now();

        List<ContractInstallment> overdueInstallments = installmentRepository
            .findByStatusAndDeadlineDateBefore(ContractInstallment.InstallmentStatus.PENDING, today);

        for (ContractInstallment installment : overdueInstallments) {
            log.info("Applying penalty to installment: {}", installment.getId());

            BigDecimal previousAmount = installment.getAmountDue();
            BigDecimal penalty = penaltyCalculator.calculatePenalty(previousAmount);
            BigDecimal newAmount = penaltyCalculator.calculateNewAmount(previousAmount, penalty);

            // Update installment
            installment.setStatus(ContractInstallment.InstallmentStatus.OVERDUE);
            installment.setAmountDue(newAmount);
            installment.setPenaltyAmount(installment.getPenaltyAmount().add(penalty));
            installmentRepository.save(installment);

            // Save penalty history
            PenaltyHistory history = PenaltyHistory.builder()
                .installment(installment)
                .previousAmount(previousAmount)
                .penaltyAmount(penalty)
                .newAmount(newAmount)
                .reason("Automatic 5% monthly penalty for overdue installment")
                .build();
            penaltyRepository.save(history);

            // Update contract status
            Contract contract = installment.getContract();
            contract.setStatus(Contract.ContractStatus.OVERDUE);
            contractRepository.save(contract);
        }

        log.info("Penalty check completed. Processed {} overdue installments.", overdueInstallments.size());
    }
}
