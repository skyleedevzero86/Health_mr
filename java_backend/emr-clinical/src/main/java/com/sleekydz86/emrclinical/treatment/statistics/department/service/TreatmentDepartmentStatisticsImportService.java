package com.sleekydz86.emrclinical.treatment.statistics.department.service;

import com.sleekydz86.emrclinical.treatment.statistics.department.entity.TreatmentDepartmentStatisticsEntity;
import com.sleekydz86.emrclinical.treatment.statistics.department.repository.TreatmentDepartmentStatisticsRepository;
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
public class TreatmentDepartmentStatisticsImportService {

    private final TreatmentDepartmentStatisticsRepository statisticsRepository;
    private final RegionCodeExtractor regionCodeExtractor;

    @Transactional
    public void importFromCsv(String csvFilePath) {
        try {
            List<TreatmentDepartmentStatisticsEntity> statistics = parseCsvFile(csvFilePath);

            statisticsRepository.deleteAll();

            statisticsRepository.saveAll(statistics);

            log.info("진료과목별 통계 데이터 임포트 완료: {}건", statistics.size());
        } catch (Exception e) {
            log.error("CSV 파일 임포트 실패", e);
            throw new RuntimeException("CSV 파일 임포트 실패: " + e.getMessage());
        }
    }

    private List<TreatmentDepartmentStatisticsEntity> parseCsvFile(String csvFilePath) {
        List<TreatmentDepartmentStatisticsEntity> statistics = new ArrayList<>();

        try (BufferedReader br = createBufferedReader(csvFilePath)) {

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] columns = parseCsvLine(line);
                if (columns.length >= 7) {
                    String year = columns[0].trim();
                    String regionName = columns[1].trim();
                    String departmentName = columns[2].trim();
                    Long patientCount = parseLong(columns[3].trim());
                    Long treatmentCount = parseLong(columns[4].trim());
                    Long medicalFee = parseLong(columns[5].trim());
                    Long benefitFee = parseLong(columns[6].trim());

                    String regionCode = regionCodeExtractor.extractRegionCode(regionName);

                    TreatmentDepartmentStatisticsEntity entity =
                            TreatmentDepartmentStatisticsEntity.builder()
                                    .statisticsYear(year)
                                    .regionCode(regionCode)
                                    .regionName(regionName)
                                    .departmentName(departmentName)
                                    .patientCount(patientCount)
                                    .treatmentCount(treatmentCount)
                                    .medicalFee(medicalFee)
                                    .benefitFee(benefitFee)
                                    .dataSource("건강보험심사평가원")
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
        if (value == null || value.isBlank()) {
            return 0L;
        }
        try {
            String cleaned = value.replaceAll("[^0-9.Ee-]", "");
            if (cleaned.contains("E") || cleaned.contains("e")) {
                double doubleValue = Double.parseDouble(cleaned);
                return (long) doubleValue;
            }
            return Long.parseLong(cleaned);
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

