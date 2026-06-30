package com.auca.contractsystem.repository;

import com.auca.contractsystem.entity.ExamPermit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamPermitRepository extends JpaRepository<ExamPermit, String> {
    List<ExamPermit> findByStudentId(String studentId);
    Page<ExamPermit> findAll(Pageable pageable);
}
