package com.auca.contractsystem.repository;

import com.auca.contractsystem.entity.TermConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermConfigRepository extends JpaRepository<TermConfig, String> {
    Optional<TermConfig> findByTermId(String termId);
}
