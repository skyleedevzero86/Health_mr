package com.sleekydz86.emrclinical.treatment.inpatient.statistics.service;

import com.sleekydz86.emrclinical.treatment.inpatient.statistics.entity.InpatientStatisticsEntity;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.repository.InpatientStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InpatientStatisticsImportService {

    private final InpatientStatisticsRepository statisticsRepository;

    @Transactional
    public void importFromXls(String xlsFilePath) {
        try {
            List<InpatientStatisticsEntity> statistics = parseXlsFile(xlsFilePath);

            statisticsRepository.deleteAll();

            statisticsRepository.saveAll(statistics);

            log.info("입원일수 통계 데이터 임포트 완료: {}건", statistics.size());
        } catch (Exception e) {
            log.error("XLS 파일 임포트 실패", e);
            throw new RuntimeException("XLS 파일 임포트 실패: " + e.getMessage());
        }
    }

    private List<InpatientStatisticsEntity> parseXlsFile(String xlsFilePath) {
        List<InpatientStatisticsEntity> statistics = new ArrayList<>();

        try (InputStream is = createInputStream(xlsFilePath);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            int startRow = findDataStartRow(sheet);
            int endRow = sheet.getLastRowNum();

            for (int i = startRow; i <= endRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                InpatientStatisticsEntity entity = parseRow(row);
                if (entity != null) {
                    statistics.add(entity);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("XLS 파일 읽기 실패", e);
        }

        return statistics;
    }

    private InputStream createInputStream(String xlsFilePath) throws IOException {
        if (xlsFilePath.startsWith("classpath:") || !Paths.get(xlsFilePath).isAbsolute()) {
            String resourcePath = xlsFilePath.startsWith("classpath:")
                    ? xlsFilePath.substring("classpath:".length())
                    : xlsFilePath;
            Resource resource = new ClassPathResource(resourcePath);
            return resource.getInputStream();
        } else {
            Path filePath = Paths.get(xlsFilePath);
            return Files.newInputStream(filePath);
        }
    }

    private int findDataStartRow(Sheet sheet) {
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell cell = row.getCell(0);
            if (cell != null) {
                String cellValue = getCellValueAsString(cell);
                if (cellValue != null && (cellValue.contains("연도") || cellValue.matches("\\d{4}"))) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    private InpatientStatisticsEntity parseRow(Row row) {
        try {
            String year = getCellValueAsString(row.getCell(0));
            String institutionType = getCellValueAsString(row.getCell(1));
            String regionCode = getCellValueAsString(row.getCell(2));
            String regionName = getCellValueAsString(row.getCell(3));
            Long visitDays = parseLong(getCellValueAsString(row.getCell(4)));
            Long benefitDays = parseLong(getCellValueAsString(row.getCell(5)));
            Long medicalFee = parseLong(getCellValueAsString(row.getCell(6)));
            Long benefitFee = parseLong(getCellValueAsString(row.getCell(7)));

            if (year == null || institutionType == null) {
                return null;
            }

            return InpatientStatisticsEntity.builder()
                    .statisticsYear(year)
                    .institutionType(institutionType)
                    .regionCode(regionCode)
                    .regionName(regionName)
                    .visitDays(visitDays)
                    .benefitDays(benefitDays)
                    .medicalFee(medicalFee)
                    .benefitFee(benefitFee)
                    .dataSource("국민건강보험공단")
                    .dataDate(LocalDate.now())
                    .build();
        } catch (Exception e) {
            log.warn("행 파싱 실패: row={}, error={}", row.getRowNum(), e.getMessage());
            return null;
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}

