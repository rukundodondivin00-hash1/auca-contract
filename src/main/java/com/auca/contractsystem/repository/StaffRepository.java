package com.auca.contractsystem.repository;

import com.auca.contractsystem.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {
    Optional<Staff> findByUsername(String username);
    Optional<Staff> findByEmail(String email);
    Optional<Staff> findByUsernameOrEmail(String username, String email);
}