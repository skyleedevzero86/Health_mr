package com.sleekydz86.support.disability.service;

import com.sleekydz86.support.disability.entity.DisabilityCareInstitutionEntity;
import com.sleekydz86.support.disability.repository.DisabilityCareInstitutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisabilityCareInstitutionImportService {

    private final DisabilityCareInstitutionRepository institutionRepository;

    @Transactional
    public void importFromCsv(String csvFilePath) {
        try {
            List<DisabilityCareInstitutionEntity> institutions = parseCsvFile(csvFilePath);

            institutionRepository.deleteAll();

            institutionRepository.saveAll(institutions);

            log.info("장애인 건강주치의 의료기관 데이터 임포트 완료: {}건", institutions.size());
        } catch (Exception e) {
            log.error("CSV 파일 임포트 실패", e);
            throw new RuntimeException("CSV 파일 임포트 실패: " + e.getMessage());
        }
    }

    private List<DisabilityCareInstitutionEntity> parseCsvFile(String csvFilePath) {
        List<DisabilityCareInstitutionEntity> institutions = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(
                Paths.get(csvFilePath), StandardCharsets.UTF_8)) {

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] columns = parseCsvLine(line);
                if (columns.length >= 4) {
                    String institutionType = columns[0].trim();
                    String institutionName = columns[1].trim();
                    String serviceType = columns[2].trim();
                    String address = columns[3].trim();

                    String[] addressParts = extractAddressParts(address);

                    DisabilityCareInstitutionEntity entity =
                        DisabilityCareInstitutionEntity.builder()
                            .institutionType(institutionType)
                            .institutionName(institutionName)
                            .serviceType(serviceType)
                            .address(address)
                            .sido(addressParts[0])
                            .sigungu(addressParts[1])
                            .isActive(true)
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

    private String[] extractAddressParts(String address) {
        String sido = null;
        String sigungu = null;

        String[] sidoKeywords = {"서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시", 
                                  "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원도", 
                                  "충청북도", "충청남도", "전라북도", "전라남도", "경상북도", "경상남도", "제주특별자치도"};

        for (String keyword : sidoKeywords) {
            if (address.contains(keyword)) {
                sido = keyword;
                int sidoIndex = address.indexOf(keyword);
                String afterSido = address.substring(sidoIndex + keyword.length()).trim();
                String[] parts = afterSido.split("\\s+");
                if (parts.length > 0) {
                    sigungu = parts[0].trim();
                }
                break;
            }
        }

        return new String[]{sido, sigungu};
    }
}

