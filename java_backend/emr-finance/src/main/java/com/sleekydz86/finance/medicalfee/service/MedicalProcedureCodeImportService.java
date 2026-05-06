package com.sleekydz86.finance.medicalfee.service;

import com.sleekydz86.finance.medicalfee.entity.MedicalProcedureCodeStatisticsEntity;
import com.sleekydz86.finance.medicalfee.repository.MedicalProcedureCodeStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalProcedureCodeImportService {

    private final MedicalProcedureCodeStatisticsRepository statisticsRepository;

    @Transactional
    public void importFromCsv(String csvFilePath) {
        try {
            List<MedicalProcedureCodeStatisticsEntity> statistics = parseCsvFile(csvFilePath);

            statisticsRepository.deleteAll();

            statisticsRepository.saveAll(statistics);

            log.info("수가코드 통계 데이터 임포트 완료: {}건", statistics.size());
        } catch (Exception e) {
            log.error("CSV 파일 임포트 실패", e);
            throw new RuntimeException("CSV 파일 임포트 실패: " + e.getMessage());
        }
    }

    private List<MedicalProcedureCodeStatisticsEntity> parseCsvFile(String csvFilePath) {
        List<MedicalProcedureCodeStatisticsEntity> statistics = new ArrayList<>();

        try (BufferedReader br = createBufferedReader(csvFilePath)) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] columns = parseCsvLine(line);
                if (columns.length >= 5) {
                    String procedureCode = columns[0].trim();
                    String treatmentYear = columns[1].trim();
                    String institutionType = columns[2].trim();
                    Long patientCount = parseLong(columns[3].trim());
                    Long treatmentCount = parseLong(columns[4].trim());

                    MedicalProcedureCodeStatisticsEntity entity =
                        MedicalProcedureCodeStatisticsEntity.builder()
                            .procedureCode(procedureCode)
                            .treatmentYear(treatmentYear)
                            .institutionType(institutionType)
                            .patientCount(patientCount)
                            .treatmentCount(treatmentCount)
                            .dataSource("국민건강보험공단")
                            .dataDate(LocalDate.now())
                            .build();

                    statistics.add(entity);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV 파일 읽기 실패", e);
        }

        return statistics;
    }

    private String[] parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder currentColumn = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                columns.add(currentColumn.toString());
                currentColumn = new StringBuilder();
            } else {
                currentColumn.append(c);
            }
        }
        columns.add(currentColumn.toString());

        return columns.toArray(new String[0]);
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private BufferedReader createBufferedReader(String csvFilePath) throws IOException {
        if (csvFilePath.startsWith("classpath:") || !Paths.get(csvFilePath).isAbsolute()) {
            String resourcePath = csvFilePath.startsWith("classpath:") 
                    ? csvFilePath.substring("classpath:".length()) 
                    : csvFilePath;
            Resource resource = new ClassPathResource(resourcePath);
            return new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        } else {
            Path filePath = Paths.get(csvFilePath);
            return Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
        }
    }
}

