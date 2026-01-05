package com.sleekydz86.support.disability.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.support.disability.dto.CareInstitutionResponse;
import com.sleekydz86.support.disability.dto.CareInstitutionSearchRequest;
import com.sleekydz86.support.disability.service.DisabilityCareInstitutionImportService;
import com.sleekydz86.support.disability.service.DisabilityCareInstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/disability/care-institution")
@RequiredArgsConstructor
public class DisabilityCareInstitutionController {

    private final DisabilityCareInstitutionService careInstitutionService;
    private final DisabilityCareInstitutionImportService importService;

    @GetMapping("/search")
    @AuthRole(roles = {"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> searchInstitutions(
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String sido,
            @RequestParam(required = false) String institutionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        CareInstitutionSearchRequest request = CareInstitutionSearchRequest.builder()
            .serviceType(serviceType)
            .sido(sido)
            .institutionType(institutionType)
            .page(page)
            .size(size)
            .build();

        Page<CareInstitutionResponse> results =
            careInstitutionService.searchInstitutions(request);

        return ResponseEntity.ok(Map.of(
            "message", "검색 성공",
            "data", results
        ));
    }

    @PostMapping("/import")
    @AuthRole(roles = {"ADMIN"})
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
        String fileName = "disability_care_institution_" + System.currentTimeMillis() + ".csv";
        Path tempPath = Paths.get(tempDir, fileName);
        Files.write(tempPath, file.getBytes());
        return tempPath.toString();
    }
}

