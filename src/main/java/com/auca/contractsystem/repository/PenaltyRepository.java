package com.auca.contractsystem.repository;

import com.auca.contractsystem.entity.PenaltyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<PenaltyHistory, String> {
    List<PenaltyHistory> findByInstallmentId(String installmentId);
}
