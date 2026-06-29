package com.auca.contractsystem.service;

import com.auca.contractsystem.dto.TermConfigDto;
import com.auca.contractsystem.entity.TermConfig;
import com.auca.contractsystem.entity.TermInstallmentConfig;
import com.auca.contractsystem.repository.TermConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.auca.contractsystem.exception.ContractException;

@Service
@RequiredArgsConstructor
public class TermConfigService {

    private final TermConfigRepository configRepository;

    @Transactional(readOnly = true)
    public List<TermConfigDto> getAllConfigs() {
        return configRepository.findAll().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TermConfigDto> getConfigByTermId(String termId) {
        return configRepository.findByTermId(termId).map(this::mapToDto);
    }

    @Transactional
    public TermConfigDto saveOrUpdateConfig(TermConfigDto dto) {
        TermConfig config = configRepository.findByTermId(dto.getTermId())
            .orElse(new TermConfig());
            
        config.setTermId(dto.getTermId());
        config.setPenaltyPercentage(dto.getPenaltyPercentage());
        config.setInitialPaymentPercentage(dto.getInitialPaymentPercentage() != null ? dto.getInitialPaymentPercentage() : new BigDecimal("100.00"));

        BigDecimal totalPercentage = config.getInitialPaymentPercentage();

        if (config.getInstallments() != null) {
            config.getInstallments().clear();
        } else {
            config.setInstallments(new java.util.ArrayList<>());
        }

        if (dto.getInstallments() != null) {
            for (com.auca.contractsystem.dto.TermInstallmentConfigDto iDto : dto.getInstallments()) {
                if (iDto.getDeadlineDate().isBefore(LocalDate.now())) {
                    throw new ContractException("Installment deadlines cannot be in the past.");
                }
                totalPercentage = totalPercentage.add(iDto.getPercentage());
                
                TermInstallmentConfig iConfig = TermInstallmentConfig.builder()
                    .installmentNumber(iDto.getInstallmentNumber())
                    .percentage(iDto.getPercentage())
                    .deadlineDate(iDto.getDeadlineDate())
                    .termConfig(config)
                    .build();
                config.getInstallments().add(iConfig);
            }
        }
        
        if (totalPercentage.compareTo(new BigDecimal("100.00")) != 0 && totalPercentage.compareTo(new BigDecimal("100")) != 0) {
            throw new ContractException("Total percentage (initial payment + installments) must equal 100%. Currently: " + totalPercentage + "%");
        }
        
        TermConfig saved = configRepository.save(config);
        return mapToDto(saved);
    }

    private TermConfigDto mapToDto(TermConfig config) {
        return TermConfigDto.builder()
            .id(config.getId())
            .termId(config.getTermId())
            .penaltyPercentage(config.getPenaltyPercentage())
            .initialPaymentPercentage(config.getInitialPaymentPercentage())
            .installments(config.getInstallments() != null ? config.getInstallments().stream().map(i -> 
                com.auca.contractsystem.dto.TermInstallmentConfigDto.builder()
                    .id(i.getId())
                    .installmentNumber(i.getInstallmentNumber())
                    .percentage(i.getPercentage())
                    .deadlineDate(i.getDeadlineDate())
                    .build()
            ).collect(Collectors.toList()) : new java.util.ArrayList<>())
            .build();
    }
}
