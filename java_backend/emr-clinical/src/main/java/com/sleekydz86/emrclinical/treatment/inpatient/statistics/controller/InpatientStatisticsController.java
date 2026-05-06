package com.sleekydz86.emrclinical.treatment.inpatient.statistics.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.dto.InpatientStatisticsByRegionResponse;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.dto.InpatientStatisticsByYearResponse;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.dto.InpatientStatisticsRequest;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.dto.InpatientStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.service.InpatientStatisticsImportService;
import com.sleekydz86.emrclinical.treatment.inpatient.statistics.service.InpatientStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/treatment/inpatient-statistics")
@RequiredArgsConstructor
public class InpatientStatisticsController {

    private final InpatientStatisticsService statisticsService;
    private final InpatientStatisticsImportService importService;

    @GetMapping("/search")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam String year,
            @RequestParam(required = false) String institutionType,
            @RequestParam(required = false) String regionCode) {

        InpatientStatisticsRequest request = InpatientStatisticsRequest.builder()
                .year(year)
                .institutionType(institutionType)
                .regionCode(regionCode)
                .build();

        InpatientStatisticsResponse response = statisticsService.getStatistics(request);

        return ResponseEntity.ok(Map.of(
                "message", "조회 성공",
                "data", response
        ));
    }

    @GetMapping("/by-year")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getStatisticsByYear(
            @RequestParam String year) {

        InpatientStatisticsByYearResponse response = statisticsService.getStatisticsByYear(year);

        return ResponseEntity.ok(Map.of(
                "message", "조회 성공",
                "data", response
        ));
    }

    @GetMapping("/by-region")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getStatisticsByRegion(
            @RequestParam String year,
            @RequestParam String regionCode) {

        InpatientStatisticsByRegionResponse response =
                statisticsService.getStatisticsByRegion(year, regionCode);

        return ResponseEntity.ok(Map.of(
                "message", "조회 성공",
                "data", response
        ));
    }

    @PostMapping("/import")
    @AuthRole({"ADMIN"})
    public ResponseEntity<Map<String, Object>> importFromXls(
            @RequestParam("file") MultipartFile file) {
        try {
            String tempFilePath = saveTempFile(file);
            importService.importFromXls(tempFilePath);

            Files.deleteIfExists(Paths.get(tempFilePath));

            return ResponseEntity.ok(Map.of(
                    "message", "데이터 임포트 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "데이터 임포트 실패",
                    "error", e.getMessage()
            ));
        }
    }

    private String saveTempFile(MultipartFile file) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = "inpatient_statistics_" + System.currentTimeMillis() + ".xls";
        Path tempPath = Paths.get(tempDir, fileName);
        Files.write(tempPath, file.getBytes());
        return tempPath.toString();
    }
}

