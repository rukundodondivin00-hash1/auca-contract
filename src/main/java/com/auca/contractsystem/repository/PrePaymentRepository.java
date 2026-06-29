package com.auca.contractsystem.repository;

import com.auca.contractsystem.entity.PrePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PrePaymentRepository extends JpaRepository<PrePayment, String> {
    List<PrePayment> findByStudentId(String studentId);
    List<PrePayment> findByStudentIdAndTermId(String studentId, String termId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PrePayment p WHERE p.studentId = :studentId AND p.feeType != 'INSTALLMENT_PAYMENT'")
    BigDecimal sumAmountByStudentId(@Param("studentId") String studentId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PrePayment p WHERE p.studentId = :studentId AND p.termId = :termId AND p.feeType != 'INSTALLMENT_PAYMENT'")
    BigDecimal sumAmountByStudentIdAndTermId(@Param("studentId") String studentId, @Param("termId") String termId);
}
