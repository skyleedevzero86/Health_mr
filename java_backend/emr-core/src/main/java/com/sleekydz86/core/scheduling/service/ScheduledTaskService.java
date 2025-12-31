package com.sleekydz86.core.scheduling.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ScheduledTaskService {

    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyStatistics() {
        log.info("일일 통계 생성 작업 시작");
        // 통계 생성 로직 구현 예정
    }

    @Scheduled(cron = "0 0 0 ? * MON")
    public void generateWeeklyReport() {
        log.info("주간 보고서 생성 작업 시작");
        // 보고서 생성 로직 구현 예정
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        log.info("만료 토큰 정리 작업 시작");
        // 토큰 정리 로직 RefreshTokenService 활용 구현 예정
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupTempFiles() {
        log.info("임시 파일 정리 작업 시작");
        // 임시 파일 정리 FileStorageService 활용해서 로직 구현할예정
    }
}

