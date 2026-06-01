package com.auca.contractsystem.repository;

import com.auca.contractsystem.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, String> {
    Optional<Contract> findByStudentIdAndTermId(String studentId, String termId);
    List<Contract> findByStudentId(String studentId);
    List<Contract> findByStatus(Contract.ContractStatus status);
    List<Contract> findAllByOrderByCreatedAtDesc();
}
