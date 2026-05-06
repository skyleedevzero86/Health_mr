package com.sleekydz86.support.healthcheckup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "health-checkup.institution.csv.auto-import", havingValue = "true", matchIfMissing = true)
public class HealthCheckupInstitutionImportScheduler {

    private final HealthCheckupInstitutionImportService importService;

    @Value("${health-checkup.institution.csv.file-path:/data/health-checkup-institution.csv}")
    private String csvFilePath;

    @Scheduled(cron = "${health-checkup.institution.csv.import-schedule:0 0 3 1 * ?}")
    public void scheduledImport() {
        try {
            importService.importFromCsv(csvFilePath);
            log.info("검진기관 정보 데이터 자동 임포트 완료");
        } catch (Exception e) {
            log.error("검진기관 정보 데이터 자동 임포트 실패", e);
        }
    }
}

