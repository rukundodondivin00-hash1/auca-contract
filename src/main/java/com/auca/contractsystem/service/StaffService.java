package com.auca.contractsystem.service;

import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.dto.staff.*;
import com.auca.contractsystem.entity.*;
import com.auca.contractsystem.exception.AuthException;
import com.auca.contractsystem.exception.ResourceNotFoundException;
import com.auca.contractsystem.security.JwtUtil;
import com.auca.contractsystem.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final ContractRepository contractRepository;
    private final InstallmentRepository installmentRepository;
    private final PenaltyRepository penaltyRepository;
    private final UserRepository userRepository;
    private final PrePaymentRepository prePaymentRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(String usernameOrEmail, String password) {
        User user = userRepository.findByEmail(usernameOrEmail)
            .orElseThrow(() -> new AuthException("Invalid staff credentials"));
        if (!password.equals(user.getPassword()) && !passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("Invalid staff credentials");
        }
        String role = user.getRole();
        String token = jwtUtil.generateToken(user.getEmail(), role);
        return LoginResponse.builder()
            .token(token)
            .username(user.getEmail())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .role(role)
            .build();
    }

    @Transactional
    public LoginResponse signup(StaffSignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthException("Email already exists");
        }
        User user = User.builder()
            .email(request.getEmail())
            .password(request.getPassword())
            .fullName(request.getFullName())
            .role("ADMIN")
            .build();
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return LoginResponse.builder()
            .token(token)
            .username(user.getEmail())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }

    public Page<StaffContractDto> getAllContracts(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Contract> contractPage = contractRepository.findAll(pageable);
        return contractPage.map(this::toStaffContractDto);
    }

    public StaffContractDto getContractById(String id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
        return toStaffContractDto(contract);
    }

    public Page<StaffContractDto> getContractsByStudentPaginated(String studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Contract> contractPage = contractRepository.findByStudentId(studentId, pageable);
        return contractPage.map(this::toStaffContractDto);
    }

    public Page<StaffContractDto> getContractsByStatusPaginated(Contract.ContractStatus status, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Contract> contractPage = contractRepository.findByStatus(status, pageable);
        return contractPage.map(this::toStaffContractDto);
    }

    @Transactional
    public StaffContractDto updateContractStatus(String id, Contract.ContractStatus status) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
        contract.setStatus(status);
        Contract saved = contractRepository.save(contract);
        return toStaffContractDto(saved);
    }

    @Transactional
    public void deleteContract(String id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
        contractRepository.delete(contract);
    }

    public Page<StaffInstallmentDto> getAllInstallments(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContractInstallment> installmentPage = installmentRepository.findAll(pageable);
        return installmentPage.map(this::toStaffInstallmentDto);
    }

    public List<StaffInstallmentDto> getInstallmentsByContract(String contractId) {
        return installmentRepository.findByContractId(contractId)
                .stream().map(this::toStaffInstallmentDto).collect(Collectors.toList());
    }

    @Transactional
    public StaffInstallmentDto updateInstallmentStatus(String id, ContractInstallment.InstallmentStatus status) {
        ContractInstallment installment = installmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Installment not found: " + id));
        installment.setStatus(status);
        if (status == ContractInstallment.InstallmentStatus.PAID) {
            installment.setPaidAt(java.time.LocalDateTime.now());
        }
        ContractInstallment saved = installmentRepository.save(installment);
        return toStaffInstallmentDto(saved);
    }

    @Transactional
    public StaffInstallmentDto waivePenalty(String id) {
        ContractInstallment installment = installmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Installment not found: " + id));
        BigDecimal currentPenalty = installment.getPenaltyAmount();
        if (currentPenalty.compareTo(BigDecimal.ZERO) > 0) {
            installment.setPenaltyAmount(BigDecimal.ZERO);
            installmentRepository.save(installment);
        }
        return toStaffInstallmentDto(installment);
    }

    public Page<StaffPenaltyDto> getAllPenaltyHistory(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PenaltyHistory> penaltyPage = penaltyRepository.findAll(pageable);
        return penaltyPage.map(this::toStaffPenaltyDto);
    }

    public List<StaffPenaltyDto> getPenaltyHistoryByInstallment(String installmentId) {
        return penaltyRepository.findByInstallmentId(installmentId)
                .stream().map(this::toStaffPenaltyDto).collect(Collectors.toList());
    }

    public List<StaffPenaltyDto> getPenaltyHistoryByContract(String contractId) {
        List<ContractInstallment> installments = installmentRepository.findByContractId(contractId);
        return installments.stream()
                .flatMap(i -> penaltyRepository.findByInstallmentId(i.getId()).stream())
                .map(this::toStaffPenaltyDto)
                .collect(Collectors.toList());
    }

    public Page<StaffStudentSummaryDto> searchStudents(int page, int size, String keyword) {
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

    public StaffStudentSummaryDto getStudentSummary(String studentId) {
        List<Contract> contracts = contractRepository.findByStudentId(studentId);
        if (contracts.isEmpty()) {
            throw new ResourceNotFoundException("No contracts found for student: " + studentId);
        }
        return toStudentSummaryFromContracts(studentId, contracts);
    }

    @Transactional
    public List<StaffContractDto> bulkUpdateContractStatus(List<String> contractIds, Contract.ContractStatus status) {
        List<Contract> contracts = contractRepository.findAllById(contractIds);
        contracts.forEach(c -> c.setStatus(status));
        contractRepository.saveAll(contracts);
        return contracts.stream().map(this::toStaffContractDto).collect(Collectors.toList());
    }

    private StaffContractDto toStaffContractDto(Contract c) {
        List<ContractInstallment> installments = installmentRepository.findByContractId(c.getId());
        BigDecimal totalPaid = installments.stream()
                .map(i -> i.getAmountPaid() != null ? i.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPenalty = installments.stream()
                .map(i -> i.getPenaltyAmount() != null ? i.getPenaltyAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return StaffContractDto.builder()
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

    private StaffInstallmentDto toStaffInstallmentDto(ContractInstallment i) {
        Contract contract = i.getContract();
        return StaffInstallmentDto.builder()
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
                .studentId(contract != null ? contract.getStudentId() : null)
                .studentName(contract != null ? contract.getStudentName() : null)
                .termId(contract != null ? contract.getTermId() : null)
                .build();
    }

    private StaffPenaltyDto toStaffPenaltyDto(PenaltyHistory p) {
        ContractInstallment installment = p.getInstallment();
        Contract contract = installment != null ? installment.getContract() : null;
        return StaffPenaltyDto.builder()
                .id(p.getId())
                .installmentId(installment != null ? installment.getId() : null)
                .contractId(contract != null ? contract.getId() : null)
                .studentId(contract != null ? contract.getStudentId() : null)
                .studentName(contract != null ? contract.getStudentName() : null)
                .previousAmount(p.getPreviousAmount())
                .penaltyAmount(p.getPenaltyAmount())
                .newAmount(p.getNewAmount())
                .reason(p.getReason())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private StaffStudentSummaryDto toStudentSummary(Contract contract) {
        return toStudentSummaryFromContracts(contract.getStudentId(), contractRepository.findByStudentId(contract.getStudentId()));
    }

    private StaffStudentSummaryDto toStudentSummaryFromContracts(String studentId, List<Contract> contracts) {
        BigDecimal totalFees = contracts.stream()
                .map(c -> c.getTotalFees() != null ? c.getTotalFees() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = prePaymentRepository.sumAmountByStudentId(studentId);
        BigDecimal totalRemaining = totalFees.subtract(totalPaid);
        boolean hasActive = contracts.stream().anyMatch(c -> c.getStatus() == Contract.ContractStatus.ACTIVE);

        Contract first = contracts.get(0);
        return StaffStudentSummaryDto.builder()
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
                .transactions(prePaymentRepository.findByStudentId(studentId))
                .build();
    }
}
