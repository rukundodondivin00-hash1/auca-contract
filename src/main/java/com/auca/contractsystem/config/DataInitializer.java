package com.auca.contractsystem.config;

import com.auca.contractsystem.entity.Staff;
import com.auca.contractsystem.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (staffRepository.count() == 0) {
            Staff defaultStaff = Staff.builder()
                .username("staff")
                .password(passwordEncoder.encode("123"))
                .fullName("System Staff")
                .email("staff@auca.ac.rw")
                .role("ROLE_STAFF")
                .build();
            staffRepository.save(defaultStaff);
            System.out.println("Seeded default staff user: username 'staff', password '123'");

            Staff adminLegacy = Staff.builder()
                .username("admin")
                .password(passwordEncoder.encode("123"))
                .fullName("Legacy Admin")
                .email("admin@auca.ac.rw")
                .role("ROLE_STAFF")
                .build();
            staffRepository.save(adminLegacy);
            System.out.println("Seeded legacy admin user: username 'admin', password '123'");
        }
    }
}
