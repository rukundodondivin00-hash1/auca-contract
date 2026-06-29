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

    /**
     * Daily job at midnight: runs penalty check + sends daily countdown reminders (10 days -> 1 day before).
     * Cron: 0 0 0 * * *  (midnight every day)
     * For testing: 0 * * * * * (every minute)
     */
    @Scheduled(cron = "0 * * * * *") // Changed to every minute for testing
    public void runDailyPenaltyCheck() {
        log.info("Scheduled penalty check started");

        // 1. Send countdown reminders (10 days out down to 1 day before)
        sendCountdownReminders();

        // 2. Apply penalties for overdue installments
        List<ContractInstallment> penalized = penaltyService.checkAndApplyPenalties();

        // 3. Notify students & staff about applied penalties
        if (penalized != null) {
            for (ContractInstallment installment : penalized) {
                String studentId = installment.getContract().getStudentId();
                NotificationMessage msg = NotificationMessage.builder()
                    .title("Penalty Applied")
                    .message("A penalty has been applied to your installment due to a missed deadline.")
                    .type("PENALTY")
                    .contractId(installment.getContract().getId())
                    .studentId(studentId)
                    .timestamp(LocalDateTime.now())
                    .build();
                messagingTemplate.convertAndSend("/topic/notifications/" + studentId, msg);

                NotificationMessage staffMsg = NotificationMessage.builder()
                    .title("Penalty Automatically Applied")
                    .message("Penalty applied for student " + studentId + " on contract " + installment.getContract().getId())
                    .type("PENALTY")
                    .contractId(installment.getContract().getId())
                    .studentId(studentId)
                    .timestamp(LocalDateTime.now())
                    .build();
                messagingTemplate.convertAndSend("/topic/staff/notifications", staffMsg);
            }
        }
    }

    /**
     * Four urgent reminders on the deadline day itself.
     * Runs at 7:00, 10:00, 13:00 and 17:00 every day.
     */
    @Scheduled(cron = "0 * * * * *") // Changed to every minute for testing
    public void sendDueTodayReminders() {
        log.info("Sending due-today reminders ({})", LocalDateTime.now());
        sendDueTodayNotifications();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    protected void sendCountdownReminders() {
        LocalDate today = LocalDate.now();

        List<ContractInstallment.InstallmentStatus> activeStatuses = List.of(
            ContractInstallment.InstallmentStatus.PENDING,
            ContractInstallment.InstallmentStatus.PARTIALLY_PAID
        );

        // Send a reminder for each of the 10 days leading up to (but not including) the deadline
        for (int daysLeft = 10; daysLeft >= 1; daysLeft--) {
            LocalDate targetDate = today.plusDays(daysLeft);
            List<ContractInstallment> due = installmentRepository
                .findByStatusInAndDeadlineDate(activeStatuses, targetDate);

            for (ContractInstallment installment : due) {
                String studentId = installment.getContract().getStudentId();
                String type   = daysLeft <= 3 ? "WARNING" : "INFO";
                String title  = daysLeft == 1
                    ? "Installment Due TOMORROW"
                    : "Installment Due in " + daysLeft + " Days";
                String message = daysLeft == 1
                    ? "Urgent: Your installment payment is due TOMORROW. Pay today to avoid a penalty."
                    : "Reminder: Your installment payment is due in " + daysLeft + " days. Please prepare your payment.";

                NotificationMessage msg = NotificationMessage.builder()
                    .title(title)
                    .message(message)
                    .type(type)
                    .contractId(installment.getContract().getId())
                    .studentId(studentId)
                    .timestamp(LocalDateTime.now())
                    .build();
                messagingTemplate.convertAndSend("/topic/notifications/" + studentId, msg);
            }
        }
    }

    @Transactional(readOnly = true)
    protected void sendDueTodayNotifications() {
        LocalDate today = LocalDate.now();

        List<ContractInstallment.InstallmentStatus> activeStatuses = List.of(
            ContractInstallment.InstallmentStatus.PENDING,
            ContractInstallment.InstallmentStatus.PARTIALLY_PAID
        );

        List<ContractInstallment> dueToday = installmentRepository
            .findByStatusInAndDeadlineDate(activeStatuses, today);

        for (ContractInstallment installment : dueToday) {
            String studentId = installment.getContract().getStudentId();
            NotificationMessage msg = NotificationMessage.builder()
                .title("Installment Due TODAY")
                .message("CRITICAL: Your installment payment is due TODAY. Pay immediately to avoid a penalty being applied.")
                .type("PENALTY")
                .contractId(installment.getContract().getId())
                .studentId(studentId)
                .timestamp(LocalDateTime.now())
                .build();
            messagingTemplate.convertAndSend("/topic/notifications/" + studentId, msg);
        }
    }
}
