package com.sleekydz86.emrclinical.treatment.statistics.department.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.emrclinical.treatment.statistics.department.dto.*;
import com.sleekydz86.emrclinical.treatment.statistics.department.service.TreatmentDepartmentStatisticsImportService;
import com.sleekydz86.emrclinical.treatment.statistics.department.service.TreatmentDepartmentStatisticsService;
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
@RequestMapping("/api/treatment/department-statistics")
@RequiredArgsConstructor
public class TreatmentDepartmentStatisticsController {

    private final TreatmentDepartmentStatisticsService statisticsService;
    private final TreatmentDepartmentStatisticsImportService importService;

    @GetMapping("/search")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam String year,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String regionCode) {

        TreatmentDepartmentStatisticsRequest request = TreatmentDepartmentStatisticsRequest.builder()
                .year(year)
                .departmentName(departmentName)
                .regionCode(regionCode)
                .build();

        TreatmentDepartmentStatisticsResponse response = statisticsService.getStatistics(request);

        return ResponseEntity.ok(Map.of(
                "message", "조회 성공",
                "data", response
        ));
    }

    @GetMapping("/by-year")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getStatisticsByYear(
            @RequestParam String year) {

        TreatmentDepartmentStatisticsByYearResponse response =
                statisticsService.getStatisticsByYear(year);

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

        TreatmentDepartmentStatisticsByRegionResponse response =
                statisticsService.getStatisticsByRegion(year, regionCode);

        return ResponseEntity.ok(Map.of(
                "message", "조회 성공",
                "data", response
        ));
    }

    @GetMapping("/by-department")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getStatisticsByDepartment(
            @RequestParam String year,
            @RequestParam String departmentName) {

        TreatmentDepartmentStatisticsByDepartmentResponse response =
                statisticsService.getStatisticsByDepartment(year, departmentName);

        return ResponseEntity.ok(Map.of(
                "message", "조회 성공",
                "data", response
        ));
    }

    @PostMapping("/import")
    @AuthRole({"ADMIN"})
    public ResponseEntity<Map<String, Object>> importFromCsv(
            @RequestParam("file") MultipartFile file) {
        try {
            String tempFilePath = saveTempFile(file);
            importService.importFromCsv(tempFilePath);

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
        String fileName = "treatment_department_statistics_" + System.currentTimeMillis() + ".csv";
        Path tempPath = Paths.get(tempDir, fileName);
        Files.write(tempPath, file.getBytes());
        return tempPath.toString();
    }
}

