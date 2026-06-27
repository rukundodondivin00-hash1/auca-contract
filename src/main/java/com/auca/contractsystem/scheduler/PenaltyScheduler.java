package com.auca.contractsystem.scheduler;

import com.auca.contractsystem.entity.ContractInstallment;
import com.auca.contractsystem.dto.NotificationMessage;
import com.auca.contractsystem.repository.InstallmentRepository;
import com.auca.contractsystem.service.PenaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PenaltyScheduler {

    private final PenaltyService penaltyService;
    private final InstallmentRepository installmentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Runs every minute for testing (was: 0 0 0 * * *)
    @Scheduled(cron = "0 * * * * *")
    public void runDailyPenaltyCheck() {
        log.info("Scheduled penalty check started");
        
        // 1. Send reminders for upcoming deadlines
        sendReminders();

        // 2. Apply penalties for overdue ones
        List<ContractInstallment> penalized = penaltyService.checkAndApplyPenalties();
        
        // 3. Send notifications for applied penalties
        if (penalized != null) {
            for (ContractInstallment installment : penalized) {
                String studentId = installment.getContract().getStudentId();
                NotificationMessage msg = NotificationMessage.builder()
                    .title("Penalty Applied")
                    .message("A 5% penalty has been applied to your installment due to missed deadline.")
                    .type("PENALTY")
                    .contractId(installment.getContract().getId())
                    .studentId(studentId)
                    .timestamp(LocalDateTime.now())
                    .build();
                messagingTemplate.convertAndSend("/topic/notifications/" + studentId, msg);
                
                // Admin Notification
                NotificationMessage adminMsg = NotificationMessage.builder()
                    .title("Penalty Automatically Applied")
                    .message("5% penalty applied for student " + studentId + " on contract " + installment.getContract().getId())
                    .type("PENALTY")
                    .contractId(installment.getContract().getId())
                    .studentId(studentId)
                    .timestamp(LocalDateTime.now())
                    .build();
                messagingTemplate.convertAndSend("/topic/admin/notifications", adminMsg);
            }
        }
    }

    @Transactional(readOnly = true)
    protected void sendReminders() {
        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);
        LocalDate in3Days = today.plusDays(3);
        LocalDate in1Day = today.plusDays(1);

        // 7 Days
        List<ContractInstallment> dueIn7Days = installmentRepository
            .findByStatusAndDeadlineDate(ContractInstallment.InstallmentStatus.PENDING, in7Days);
        
        for (ContractInstallment installment : dueIn7Days) {
            String studentId = installment.getContract().getStudentId();
            NotificationMessage msg = NotificationMessage.builder()
                .title("Upcoming Installment")
                .message("Reminder: Your installment is due in 7 days.")
                .type("INFO")
                .contractId(installment.getContract().getId())
                .studentId(studentId)
                .timestamp(LocalDateTime.now())
                .build();
            messagingTemplate.convertAndSend("/topic/notifications/" + studentId, msg);
        }

        // 3 Days
        List<ContractInstallment> dueIn3Days = installmentRepository
            .findByStatusAndDeadlineDate(ContractInstallment.InstallmentStatus.PENDING, in3Days);
        
        for (ContractInstallment installment : dueIn3Days) {
            String studentId = installment.getContract().getStudentId();
            NotificationMessage msg = NotificationMessage.builder()
                .title("Installment Due Soon")
                .message("Urgent: Your installment is due in exactly 3 days. Please pay to avoid penalties.")
                .type("WARNING")
                .contractId(installment.getContract().getId())
                .studentId(studentId)
                .timestamp(LocalDateTime.now())
                .build();
            messagingTemplate.convertAndSend("/topic/notifications/" + studentId, msg);
        }
        
        // 1 Day
        List<ContractInstallment> dueIn1Day = installmentRepository
            .findByStatusAndDeadlineDate(ContractInstallment.InstallmentStatus.PENDING, in1Day);
        
        for (ContractInstallment installment : dueIn1Day) {
            String studentId = installment.getContract().getStudentId();
            NotificationMessage msg = NotificationMessage.builder()
                .title("Installment Due Tomorrow")
                .message("CRITICAL: Your installment is due tomorrow. Pay now to avoid a 5% penalty.")
                .type("WARNING")
                .contractId(installment.getContract().getId())
                .studentId(studentId)
                .timestamp(LocalDateTime.now())
                .build();
            messagingTemplate.convertAndSend("/topic/notifications/" + studentId, msg);
        }
    }
}
