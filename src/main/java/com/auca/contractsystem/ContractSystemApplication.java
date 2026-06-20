package com.auca.contractsystem;

import com.auca.contractsystem.entity.Admin;
import com.auca.contractsystem.repository.AdminRepository;
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
    CommandLineRunner initAdmin(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (adminRepository.findByUsername("admin").isEmpty()) {
                Admin admin = Admin.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("password"))
                    .fullName("System Administrator")
                    .email("admin@aucacontractsystem.com")
                    .role("ADMIN")
                    .build();
                adminRepository.save(admin);
            }
        };
    }
}
