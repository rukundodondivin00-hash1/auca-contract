package com.auca.contractsystem.scheduler;

import com.auca.contractsystem.service.PenaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PenaltyScheduler {

    private final PenaltyService penaltyService;

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyPenaltyCheck() {
        log.info("Scheduled penalty check started");
        penaltyService.checkAndApplyPenalties();
    }
}
