package com.auca.contractsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ContractSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContractSystemApplication.class, args);
    }
}
