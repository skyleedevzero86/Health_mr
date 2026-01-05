package com.sleekydz86.emrclinical.treatment.inpatient.statistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "treatment.inpatient-statistics.xls.auto-import", havingValue = "true", matchIfMissing = true)
public class InpatientStatisticsImportScheduler {

    private final InpatientStatisticsImportService importService;

    @Value("${treatment.inpatient-statistics.xls.directory-path:data/xls}")
    private String directoryPath;

    @Value("${treatment.inpatient-statistics.xls.file-pattern:.*_건강보험대상자 진료실적\\(시도\\)\\.xls}")
    private String filePattern;

    @Value("${treatment.inpatient-statistics.xls.processing-strategy:MERGE_ALL}")
    private String processingStrategy;

    @Scheduled(cron = "${treatment.inpatient-statistics.xls.import-schedule:0 0 3 1 * ?}")
    public void scheduledImport() {
        try {
            Path dir = Paths.get(directoryPath);
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                log.warn("XLS 디렉토리가 존재하지 않습니다: {}", directoryPath);
                return;
            }

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(filePattern);

            try (Stream<Path> paths = Files.list(dir)) {
                List<Path> matchingFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(path -> pattern.matcher(path.getFileName().toString()).matches())
                        .sorted(Comparator.comparing(path -> {
                            try {
                                return Files.getLastModifiedTime(path).toInstant();
                            } catch (IOException e) {
                                return java.time.Instant.MIN;
                            }
                        }).reversed())
                        .collect(Collectors.toList());

                if (matchingFiles.isEmpty()) {
                    log.warn("매칭되는 XLS 파일이 없습니다: pattern={}", filePattern);
                    return;
                }

                if ("LATEST_ONLY".equals(processingStrategy)) {
                    Path latestFile = matchingFiles.get(0);
                    importService.importFromXls(latestFile.toString());
                    log.info("입원일수 통계 데이터 자동 임포트 완료: file={}", latestFile.getFileName());
                } else if ("MERGE_ALL".equals(processingStrategy)) {
                    for (Path file : matchingFiles) {
                        try {
                            importService.importFromXls(file.toString());
                            log.info("입원일수 통계 데이터 임포트 완료: file={}", file.getFileName());
                        } catch (Exception e) {
                            log.error("파일 임포트 실패: file={}", file.getFileName(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("입원일수 통계 데이터 자동 임포트 실패", e);
        }
    }
}

