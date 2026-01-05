package com.sleekydz86.emrclinical.treatment.statistics.department.service;

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
@ConditionalOnProperty(name = "treatment.department-statistics.csv.auto-import", havingValue = "true", matchIfMissing = true)
public class TreatmentDepartmentStatisticsImportScheduler {

    private final TreatmentDepartmentStatisticsImportService importService;

    @Value("${treatment.department-statistics.csv.directory-path:data/csv}")
    private String directoryPath;

    @Value("${treatment.department-statistics.csv.file-pattern:.*_진료과목별.*\\.csv}")
    private String filePattern;

    @Value("${treatment.department-statistics.csv.processing-strategy:LATEST_ONLY}")
    private String processingStrategy;

    @Scheduled(cron = "${treatment.department-statistics.csv.import-schedule:0 0 3 1 * ?}")
    public void scheduledImport() {
        try {
            Path dir = Paths.get(directoryPath);
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                log.warn("CSV 디렉토리가 존재하지 않습니다: {}", directoryPath);
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
                    log.warn("매칭되는 CSV 파일이 없습니다: pattern={}", filePattern);
                    return;
                }

                if ("LATEST_ONLY".equals(processingStrategy)) {
                    Path latestFile = matchingFiles.get(0);
                    importService.importFromCsv(latestFile.toString());
                    log.info("진료과목별 통계 데이터 자동 임포트 완료: file={}", latestFile.getFileName());
                } else if ("MERGE_ALL".equals(processingStrategy)) {
                    for (Path file : matchingFiles) {
                        try {
                            importService.importFromCsv(file.toString());
                            log.info("진료과목별 통계 데이터 임포트 완료: file={}", file.getFileName());
                        } catch (Exception e) {
                            log.error("파일 임포트 실패: file={}", file.getFileName(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("진료과목별 통계 데이터 자동 임포트 실패", e);
        }
    }
}

