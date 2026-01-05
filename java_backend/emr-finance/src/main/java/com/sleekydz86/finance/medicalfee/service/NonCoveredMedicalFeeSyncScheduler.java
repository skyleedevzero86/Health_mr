package com.sleekydz86.finance.medicalfee.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "non-covered-medical-fee.sync.enabled", havingValue = "true", matchIfMissing = true)
public class NonCoveredMedicalFeeSyncScheduler {

    private final NonCoveredMedicalFeeSyncService syncService;

    @Value("${non-covered-medical-fee.sync.schedule:0 0 2 1 * ?}")
    private String schedule;

    @Scheduled(cron = "${non-covered-medical-fee.sync.schedule:0 0 2 1 * ?}")
    public void syncAllMedicalTypeFees() {
        log.info("비급여 진료비 금액 동기화 시작");
        try {
            syncService.syncAllMedicalTypeFees();
            log.info("비급여 진료비 금액 동기화 완료");
        } catch (Exception e) {
            log.error("비급여 진료비 금액 동기화 실패", e);
        }
    }
}

