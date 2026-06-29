package com.auca.contractsystem;

import com.auca.contractsystem.entity.Staff;
import com.auca.contractsystem.repository.StaffRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class ContractSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContractSystemApplication.class, args);
    }

    @Bean
    CommandLineRunner initStaff(StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (staffRepository.findByUsername("staff").isEmpty()) {
                Staff staff = Staff.builder()
                    .username("staff")
                    .password(passwordEncoder.encode("password"))
                    .fullName("System Staffistrator")
                    .email("staff@aucacontractsystem.com")
                    .role("STAFF")
                    .build();
                staffRepository.save(staff);
            }
        };
    }
}
