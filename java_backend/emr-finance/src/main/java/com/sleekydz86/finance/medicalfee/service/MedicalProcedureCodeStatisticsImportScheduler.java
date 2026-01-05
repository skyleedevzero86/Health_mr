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
@ConditionalOnProperty(name = "medical-fee.procedure-code-statistics.csv.auto-import", havingValue = "true", matchIfMissing = true)
public class MedicalProcedureCodeStatisticsImportScheduler {

    private final MedicalProcedureCodeImportService importService;

    @Value("${medical-fee.procedure-code-statistics.csv.file-path:data/국민건강보험공단_특정 수가코드(D2420 D4260) 기준 진료 현황_20241231.csv}")
    private String csvFilePath;

    @Scheduled(cron = "${medical-fee.procedure-code-statistics.csv.import-schedule:0 0 3 1 * ?}")
    public void scheduledImport() {
        try {
            importService.importFromCsv(csvFilePath);
            log.info("수가코드 통계 데이터 자동 임포트 완료");
        } catch (Exception e) {
            log.error("수가코드 통계 데이터 자동 임포트 실패", e);
        }
    }
}

