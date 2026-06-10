package com.auca.contractsystem.service;

import com.auca.contractsystem.dto.admin.AdminContractDto;
import com.auca.contractsystem.dto.admin.AdminInstallmentDto;
import com.auca.contractsystem.dto.admin.AdminPenaltyDto;
import com.auca.contractsystem.dto.admin.AdminStudentSummaryDto;
import com.auca.contractsystem.entity.Contract;
import com.auca.contractsystem.entity.ContractInstallment;
import com.auca.contractsystem.entity.PenaltyHistory;
import com.auca.contractsystem.exception.ResourceNotFoundException;
import com.auca.contractsystem.repository.ContractRepository;
import com.auca.contractsystem.repository.InstallmentRepository;
import com.auca.contractsystem.repository.PenaltyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final ContractRepository contractRepository;
    private final InstallmentRepository installmentRepository;
    private final PenaltyRepository penaltyRepository;

    public Page<AdminContractDto> getAllContracts(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Contract> contractPage = contractRepository.findAll(pageable);
        return contractPage.map(this::toAdminContractDto);
    }

    public AdminContractDto getContractById(String id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
        return toAdminContractDto(contract);
    }

    public List<AdminContractDto> getContractsByStudent(String studentId) {
        return contractRepository.findByStudentId(studentId)
                .stream().map(this::toAdminContractDto).collect(Collectors.toList());
    }

    public List<AdminContractDto> getContractsByStatus(Contract.ContractStatus status) {
        return contractRepository.findByStatus(status)
                .stream().map(this::toAdminContractDto).collect(Collectors.toList());
    }

    @Transactional
    public AdminContractDto updateContractStatus(String id, Contract.ContractStatus status) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
        contract.setStatus(status);
        Contract saved = contractRepository.save(contract);
        return toAdminContractDto(saved);
    }

    @Transactional
    public void deleteContract(String id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
        contractRepository.delete(contract);
    }

    public Page<AdminInstallmentDto> getAllInstallments(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContractInstallment> installmentPage = installmentRepository.findAll(pageable);
        return installmentPage.map(this::toAdminInstallmentDto);
    }

    public List<AdminInstallmentDto> getInstallmentsByContract(String contractId) {
        return installmentRepository.findByContractId(contractId)
                .stream().map(this::toAdminInstallmentDto).collect(Collectors.toList());
    }

    @Transactional
    public AdminInstallmentDto updateInstallmentStatus(String id, ContractInstallment.InstallmentStatus status) {
        ContractInstallment installment = installmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Installment not found: " + id));
        installment.setStatus(status);
        if (status == ContractInstallment.InstallmentStatus.PAID) {
            installment.setPaidAt(java.time.LocalDateTime.now());
        }
        ContractInstallment saved = installmentRepository.save(installment);
        return toAdminInstallmentDto(saved);
    }

    @Transactional
    public AdminInstallmentDto waivePenalty(String id) {
        ContractInstallment installment = installmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Installment not found: " + id));
        BigDecimal currentPenalty = installment.getPenaltyAmount();
        if (currentPenalty.compareTo(BigDecimal.ZERO) > 0) {
            installment.setPenaltyAmount(BigDecimal.ZERO);
            installmentRepository.save(installment);
        }
        return toAdminInstallmentDto(installment);
    }

    public Page<AdminPenaltyDto> getAllPenaltyHistory(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PenaltyHistory> penaltyPage = penaltyRepository.findAll(pageable);
        return penaltyPage.map(this::toAdminPenaltyDto);
    }

    public List<AdminPenaltyDto> getPenaltyHistoryByInstallment(String installmentId) {
        return penaltyRepository.findByInstallmentId(installmentId)
                .stream().map(this::toAdminPenaltyDto).collect(Collectors.toList());
    }

    public List<AdminPenaltyDto> getPenaltyHistoryByContract(String contractId) {
        List<ContractInstallment> installments = installmentRepository.findByContractId(contractId);
        return installments.stream()
                .flatMap(i -> penaltyRepository.findByInstallmentId(i.getId()).stream())
                .map(this::toAdminPenaltyDto)
                .collect(Collectors.toList());
    }

    public Page<AdminStudentSummaryDto> searchStudents(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Contract> contractPage;
        if (keyword == null || keyword.isBlank()) {
            contractPage = contractRepository.findAll(pageable);
        } else {
            String kw = "%" + keyword.toLowerCase() + "%";
            contractPage = contractRepository.findByStudentNameContainingIgnoreCaseOrStudentIdContainingIgnoreCase(keyword, keyword, pageable);
        }
        return contractPage.map(this::toStudentSummary);
    }

    public AdminStudentSummaryDto getStudentSummary(String studentId) {
        List<Contract> contracts = contractRepository.findByStudentId(studentId);
        if (contracts.isEmpty()) {
            throw new ResourceNotFoundException("No contracts found for student: " + studentId);
        }
        return toStudentSummaryFromContracts(studentId, contracts);
    }

    @Transactional
    public List<AdminContractDto> bulkUpdateContractStatus(List<String> contractIds, Contract.ContractStatus status) {
        List<Contract> contracts = contractRepository.findAllById(contractIds);
        contracts.forEach(c -> c.setStatus(status));
        contractRepository.saveAll(contracts);
        return contracts.stream().map(this::toAdminContractDto).collect(Collectors.toList());
    }

    private AdminContractDto toAdminContractDto(Contract c) {
        List<ContractInstallment> installments = installmentRepository.findByContractId(c.getId());
        BigDecimal totalPaid = installments.stream()
                .map(i -> i.getAmountPaid() != null ? i.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPenalty = installments.stream()
                .map(i -> i.getPenaltyAmount() != null ? i.getPenaltyAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminContractDto.builder()
                .id(c.getId())
                .studentId(c.getStudentId())
                .studentName(c.getStudentName())
                .termId(c.getTermId())
                .academicYear(c.getAcademicYear())
                .semester(c.getSemester())
                .totalFees(c.getTotalFees())
                .balanceAtSigning(c.getBalanceAtSigning())
                .amountPaidAtSigning(c.getAmountPaidAtSigning())
                .remainingAtSigning(c.getRemainingAtSigning())
                .status(c.getStatus().name())
                .agreed(c.getAgreed())
                .agreedDate(c.getAgreedDate())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .installmentCount(installments.size())
                .totalPaidOnInstallments(totalPaid)
                .totalPenaltyOnInstallments(totalPenalty)
                .build();
    }

    private AdminInstallmentDto toAdminInstallmentDto(ContractInstallment i) {
        Contract contract = i.getContract();
        return AdminInstallmentDto.builder()
                .id(i.getId())
                .contractId(contract != null ? contract.getId() : null)
                .installmentNumber(i.getInstallmentNumber())
                .deadlineDate(i.getDeadlineDate())
                .amountDue(i.getAmountDue())
                .amountPaid(i.getAmountPaid())
                .penaltyAmount(i.getPenaltyAmount())
                .status(i.getStatus().name())
                .paidAt(i.getPaidAt())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .studentName(contract != null ? contract.getStudentName() : null)
                .termId(contract != null ? contract.getTermId() : null)
                .build();
    }

    private AdminPenaltyDto toAdminPenaltyDto(PenaltyHistory p) {
        ContractInstallment installment = p.getInstallment();
        Contract contract = installment != null ? installment.getContract() : null;
        return AdminPenaltyDto.builder()
                .id(p.getId())
                .installmentId(installment != null ? installment.getId() : null)
                .contractId(contract != null ? contract.getId() : null)
                .studentName(contract != null ? contract.getStudentName() : null)
                .previousAmount(p.getPreviousAmount())
                .penaltyAmount(p.getPenaltyAmount())
                .newAmount(p.getNewAmount())
                .reason(p.getReason())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private AdminStudentSummaryDto toStudentSummary(Contract contract) {
        return toStudentSummaryFromContracts(contract.getStudentId(), contractRepository.findByStudentId(contract.getStudentId()));
    }

    private AdminStudentSummaryDto toStudentSummaryFromContracts(String studentId, List<Contract> contracts) {
        BigDecimal totalFees = contracts.stream()
                .map(c -> c.getTotalFees() != null ? c.getTotalFees() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = contracts.stream()
                .map(c -> c.getAmountPaidAtSigning() != null ? c.getAmountPaidAtSigning() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRemaining = contracts.stream()
                .map(c -> c.getRemainingAtSigning() != null ? c.getRemainingAtSigning() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean hasActive = contracts.stream().anyMatch(c -> c.getStatus() == Contract.ContractStatus.ACTIVE);

        Contract first = contracts.get(0);
        return AdminStudentSummaryDto.builder()
                .studentId(studentId)
                .studentName(first.getStudentName())
                .department(null)
                .departmentCode(null)
                .program(null)
                .contractCount(contracts.size())
                .totalFeesAcrossContracts(totalFees)
                .totalPaidAcrossContracts(totalPaid)
                .totalRemainingAcrossContracts(totalRemaining)
                .hasActiveContract(hasActive)
                .build();
    }
}
