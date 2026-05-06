package com.sleekydz86.support.healthcheckup.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.support.healthcheckup.dto.HealthCheckupInstitutionRecommendationResponse;
import com.sleekydz86.support.healthcheckup.dto.HealthCheckupInstitutionResponse;
import com.sleekydz86.support.healthcheckup.dto.HealthCheckupInstitutionSearchRequest;
import com.sleekydz86.support.healthcheckup.service.HealthCheckupInstitutionImportService;
import com.sleekydz86.support.healthcheckup.service.HealthCheckupInstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-checkup/institutions")
@RequiredArgsConstructor
public class HealthCheckupInstitutionController {

    private final HealthCheckupInstitutionService institutionService;
    private final HealthCheckupInstitutionImportService importService;

    @GetMapping("/search")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR", "PATIENT"})
    public ResponseEntity<Map<String, Object>> searchInstitutions(
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String institutionType,
            @RequestParam(required = false) String institutionName,
            @RequestParam(required = false) String sido,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        HealthCheckupInstitutionSearchRequest request =
            HealthCheckupInstitutionSearchRequest.builder()
                .regionCode(regionCode)
                .institutionType(institutionType)
                .institutionName(institutionName)
                .sido(sido)
                .page(page)
                .size(size)
                .build();

        Page<HealthCheckupInstitutionResponse> response =
            institutionService.searchInstitutions(request);

        return ResponseEntity.ok(Map.of(
            "message", "조회 성공",
            "data", response
        ));
    }

    @GetMapping("/recommend/{patientNo}")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR", "PATIENT"})
    public ResponseEntity<Map<String, Object>> recommendInstitutions(
            @PathVariable Long patientNo) {

        List<HealthCheckupInstitutionRecommendationResponse> response =
            institutionService.recommendInstitutions(patientNo);

        return ResponseEntity.ok(Map.of(
            "message", "추천 성공",
            "data", response
        ));
    }

    @GetMapping("/{institutionId}")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR", "PATIENT"})
    public ResponseEntity<Map<String, Object>> getInstitution(
            @PathVariable Long institutionId) {

        HealthCheckupInstitutionResponse response =
            institutionService.getInstitutionById(institutionId);

        return ResponseEntity.ok(Map.of(
            "message", "조회 성공",
            "data", response
        ));
    }

    @GetMapping("/region/{regionCode}")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR", "PATIENT"})
    public ResponseEntity<Map<String, Object>> getInstitutionsByRegion(
            @PathVariable String regionCode) {

        List<HealthCheckupInstitutionResponse> response =
            institutionService.getInstitutionsByRegion(regionCode);

        return ResponseEntity.ok(Map.of(
            "message", "조회 성공",
            "data", response
        ));
    }

    @GetMapping("/type/{institutionType}")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR", "PATIENT"})
    public ResponseEntity<Map<String, Object>> getInstitutionsByType(
            @PathVariable String institutionType) {

        List<HealthCheckupInstitutionResponse> response =
            institutionService.getInstitutionsByType(institutionType);

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
        String fileName = "health_checkup_institution_" + System.currentTimeMillis() + ".csv";
        Path tempPath = Paths.get(tempDir, fileName);
        Files.write(tempPath, file.getBytes());
        return tempPath.toString();
    }
}

