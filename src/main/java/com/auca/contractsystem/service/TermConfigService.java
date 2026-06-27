package com.auca.contractsystem.service;

import com.auca.contractsystem.dto.TermConfigDto;
import com.auca.contractsystem.entity.TermConfig;
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
        config.setMaxInstallments(dto.getMaxInstallments());
        config.setPenaltyPercentage(dto.getPenaltyPercentage());
        
        TermConfig saved = configRepository.save(config);
        return mapToDto(saved);
    }

    private TermConfigDto mapToDto(TermConfig config) {
        return TermConfigDto.builder()
            .id(config.getId())
            .termId(config.getTermId())
            .maxInstallments(config.getMaxInstallments())
            .penaltyPercentage(config.getPenaltyPercentage())
            .build();
    }
}
