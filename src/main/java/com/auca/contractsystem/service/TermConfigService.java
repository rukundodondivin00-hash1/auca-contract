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

        if (config.getInstallments() != null) {
            config.getInstallments().clear();
        } else {
            config.setInstallments(new java.util.ArrayList<>());
        }

        if (dto.getInstallments() != null) {
            for (com.auca.contractsystem.dto.TermInstallmentConfigDto iDto : dto.getInstallments()) {
                TermInstallmentConfig iConfig = TermInstallmentConfig.builder()
                    .installmentNumber(iDto.getInstallmentNumber())
                    .percentage(iDto.getPercentage())
                    .deadlineDate(iDto.getDeadlineDate())
                    .termConfig(config)
                    .build();
                config.getInstallments().add(iConfig);
            }
        }
        
        TermConfig saved = configRepository.save(config);
        return mapToDto(saved);
    }

    private TermConfigDto mapToDto(TermConfig config) {
        return TermConfigDto.builder()
            .id(config.getId())
            .termId(config.getTermId())
            .penaltyPercentage(config.getPenaltyPercentage())
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
