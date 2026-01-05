package com.sleekydz86.finance.medicalfee.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.finance.medicalfee.dto.ProcedureCodeStatisticsByInstitutionResponse;
import com.sleekydz86.finance.medicalfee.dto.ProcedureCodeStatisticsByYearResponse;
import com.sleekydz86.finance.medicalfee.dto.ProcedureCodeStatisticsRequest;
import com.sleekydz86.finance.medicalfee.dto.ProcedureCodeStatisticsResponse;
import com.sleekydz86.finance.medicalfee.service.MedicalProcedureCodeImportService;
import com.sleekydz86.finance.medicalfee.service.MedicalProcedureCodeStatisticsService;
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
@RequestMapping("/api/medical-fee/procedure-code-statistics")
@RequiredArgsConstructor
public class MedicalProcedureCodeStatisticsController {

    private final MedicalProcedureCodeStatisticsService statisticsService;
    private final MedicalProcedureCodeImportService importService;

    @GetMapping("/search")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam String procedureCode,
            @RequestParam(required = false) String startYear,
            @RequestParam(required = false) String endYear,
            @RequestParam(required = false) String institutionType) {
        
        ProcedureCodeStatisticsRequest request = ProcedureCodeStatisticsRequest.builder()
            .procedureCode(procedureCode)
            .startYear(startYear)
            .endYear(endYear)
            .institutionType(institutionType)
            .build();

        ProcedureCodeStatisticsResponse response = 
            statisticsService.getStatisticsByCode(request);

        return ResponseEntity.ok(Map.of(
            "message", "조회 성공",
            "data", response
        ));
    }

    @GetMapping("/by-year")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getStatisticsByYear(
            @RequestParam String procedureCode,
            @RequestParam String year) {
        
        ProcedureCodeStatisticsByYearResponse response = 
            statisticsService.getStatisticsByYear(procedureCode, year);

        return ResponseEntity.ok(Map.of(
            "message", "조회 성공",
            "data", response
        ));
    }

    @GetMapping("/by-institution")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getStatisticsByInstitution(
            @RequestParam String procedureCode,
            @RequestParam String institutionType) {
        
        ProcedureCodeStatisticsByInstitutionResponse response = 
            statisticsService.getStatisticsByInstitution(procedureCode, institutionType);

        return ResponseEntity.ok(Map.of(
            "message", "조회 성공",
            "data", response
        ));
    }

    @GetMapping("/comparison")
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Map<String, Object>> getComparison(
            @RequestParam String procedureCode1,
            @RequestParam String procedureCode2,
            @RequestParam String year) {
        
        Map<String, Object> comparison = 
            statisticsService.getComparisonStatistics(procedureCode1, procedureCode2, year);

        return ResponseEntity.ok(Map.of(
            "message", "비교 분석 성공",
            "data", comparison
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
        String fileName = "procedure_code_statistics_" + System.currentTimeMillis() + ".csv";
        Path tempPath = Paths.get(tempDir, fileName);
        Files.write(tempPath, file.getBytes());
        return tempPath.toString();
    }
}

