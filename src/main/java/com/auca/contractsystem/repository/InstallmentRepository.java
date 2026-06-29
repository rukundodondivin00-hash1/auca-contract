package com.auca.contractsystem.repository;

import com.auca.contractsystem.entity.ContractInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InstallmentRepository extends JpaRepository<ContractInstallment, String> {
    List<ContractInstallment> findByContractId(String contractId);
    List<ContractInstallment> findByStatusInAndDeadlineDateBefore(
        List<ContractInstallment.InstallmentStatus> statuses, LocalDate date);
    List<ContractInstallment> findByStatusInAndDeadlineDate(
        List<ContractInstallment.InstallmentStatus> statuses, LocalDate date);
    List<ContractInstallment> findByContractIdAndStatusNotOrderByDeadlineDateAsc(
        String contractId, ContractInstallment.InstallmentStatus status);
    
    @Query("SELECT i FROM ContractInstallment i WHERE i.contract.id = :contractId AND i.status IN ('PENDING', 'PARTIALLY_PAID') ORDER BY i.deadlineDate ASC")
    List<ContractInstallment> findByContractIdAndStatusInPENDING_OR_PARTIALLY_PAID(
        @Param("contractId") String contractId);
}
