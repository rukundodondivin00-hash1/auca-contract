package com.auca.contractsystem.repository;

import com.auca.contractsystem.entity.ContractInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InstallmentRepository extends JpaRepository<ContractInstallment, String> {
    List<ContractInstallment> findByContractId(String contractId);
    List<ContractInstallment> findByStatusAndDeadlineDateBefore(
        ContractInstallment.InstallmentStatus status, LocalDate date);
}
