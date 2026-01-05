package com.sleekydz86.support.healthcheckup.service;

import com.sleekydz86.support.healthcheckup.entity.HealthCheckupInstitutionEntity;
import com.sleekydz86.support.healthcheckup.repository.HealthCheckupInstitutionRepository;
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
public class HealthCheckupInstitutionImportService {

    private final HealthCheckupInstitutionRepository institutionRepository;

    @Transactional
    public void importFromCsv(String csvFilePath) {
        try {
            List<HealthCheckupInstitutionEntity> institutions = parseCsvFile(csvFilePath);

            institutionRepository.deleteAll();

            institutionRepository.saveAll(institutions);

            log.info("검진기관 정보 데이터 임포트 완료: {}건", institutions.size());
        } catch (Exception e) {
            log.error("CSV 파일 임포트 실패", e);
            throw new RuntimeException("CSV 파일 임포트 실패: " + e.getMessage());
        }
    }

    private List<HealthCheckupInstitutionEntity> parseCsvFile(String csvFilePath) {
        List<HealthCheckupInstitutionEntity> institutions = new ArrayList<>();

        try (BufferedReader br = createBufferedReader(csvFilePath)) {

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] columns = parseCsvLine(line);
                if (columns.length >= 4) {
                    String regionName = columns[0].trim();
                    String institutionName = columns[1].trim();
                    String institutionType = columns[2].trim();
                    String address = columns[3].trim();

                    String regionCode = extractRegionCode(regionName);
                    String sido = extractSido(regionName);
                    String sigungu = extractSigungu(address);

                    HealthCheckupInstitutionEntity entity = HealthCheckupInstitutionEntity.builder()
                            .regionCode(regionCode)
                            .regionName(regionName)
                            .institutionName(institutionName)
                            .institutionType(institutionType)
                            .address(address)
                            .sido(sido)
                            .sigungu(sigungu)
                            .isActive(true)
                            .dataSource("국민건강보험공단")
                            .dataDate(LocalDate.now())
                            .build();

                    institutions.add(entity);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV 파일 읽기 실패", e);
        }

        return institutions;
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

    private String extractRegionCode(String regionName) {
        if (regionName == null || regionName.isBlank()) {
            return null;
        }
        if (regionName.contains("서울"))
            return "11";
        if (regionName.contains("부산"))
            return "26";
        if (regionName.contains("대구"))
            return "27";
        if (regionName.contains("인천"))
            return "28";
        if (regionName.contains("광주"))
            return "29";
        if (regionName.contains("대전"))
            return "30";
        if (regionName.contains("울산"))
            return "31";
        if (regionName.contains("세종"))
            return "36";
        if (regionName.contains("경기"))
            return "41";
        if (regionName.contains("강원"))
            return "42";
        if (regionName.contains("충북"))
            return "43";
        if (regionName.contains("충남"))
            return "44";
        if (regionName.contains("전북"))
            return "45";
        if (regionName.contains("전남"))
            return "46";
        if (regionName.contains("경북"))
            return "47";
        if (regionName.contains("경남"))
            return "48";
        if (regionName.contains("제주"))
            return "50";
        return null;
    }

    private String extractSido(String regionName) {
        if (regionName == null || regionName.isBlank()) {
            return "";
        }
        if (regionName.contains("서울"))
            return "서울";
        if (regionName.contains("부산"))
            return "부산";
        if (regionName.contains("대구"))
            return "대구";
        if (regionName.contains("인천"))
            return "인천";
        if (regionName.contains("광주"))
            return "광주";
        if (regionName.contains("대전"))
            return "대전";
        if (regionName.contains("울산"))
            return "울산";
        if (regionName.contains("세종"))
            return "세종";
        if (regionName.contains("경기"))
            return "경기";
        if (regionName.contains("강원"))
            return "강원";
        if (regionName.contains("충북"))
            return "충북";
        if (regionName.contains("충남"))
            return "충남";
        if (regionName.contains("전북"))
            return "전북";
        if (regionName.contains("전남"))
            return "전남";
        if (regionName.contains("경북"))
            return "경북";
        if (regionName.contains("경남"))
            return "경남";
        if (regionName.contains("제주"))
            return "제주";
        return "";
    }

    private String extractSigungu(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }

        String[] parts = address.split(" ");
        if (parts.length >= 2) {
            return parts[1];
        }
        return "";
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
