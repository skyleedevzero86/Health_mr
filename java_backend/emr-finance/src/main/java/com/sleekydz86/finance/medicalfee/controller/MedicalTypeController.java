package com.sleekydz86.finance.medicalfee.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.finance.medicalfee.dto.MedicalTypeDetailResponse;
import com.sleekydz86.finance.medicalfee.dto.MedicalTypeRequest;
import com.sleekydz86.finance.medicalfee.dto.MedicalTypeResponse;
import com.sleekydz86.finance.medicalfee.dto.MedicalTypeUpdateRequest;
import com.sleekydz86.finance.medicalfee.service.MedicalTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicaltype")
@RequiredArgsConstructor
public class MedicalTypeController {

    private final MedicalTypeService medicalTypeService;

    @PostMapping
    @AuthRole({RoleType.ADMIN})
    public ResponseEntity<Map<String, Object>> createMedicalType(@Valid @RequestBody MedicalTypeRequest request) {
        MedicalTypeResponse response = medicalTypeService.createMedicalType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "진료 유형 생성 성공",
                "data", response
        ));
    }

    @GetMapping("/{medicalTypeId}")
    @AuthRole({RoleType.STAFF, RoleType.ADMIN, RoleType.DOCTOR})
    public ResponseEntity<Map<String, Object>> getMedicalType(@PathVariable Long medicalTypeId) {
        MedicalTypeDetailResponse response = medicalTypeService.getMedicalTypeDetailById(medicalTypeId);
        return ResponseEntity.ok(Map.of(
                "message", "진료 유형 조회 성공",
                "data", response
        ));
    }

    @GetMapping
    @AuthRole({RoleType.STAFF, RoleType.ADMIN, RoleType.DOCTOR})
    public ResponseEntity<Page<MedicalTypeResponse>> getAllMedicalTypes(Pageable pageable) {
        Page<MedicalTypeResponse> medicalTypes = medicalTypeService.getAllMedicalTypes(pageable);
        return ResponseEntity.ok(medicalTypes);
    }

    @GetMapping("/code/{code}")
    @AuthRole({RoleType.STAFF, RoleType.ADMIN, RoleType.DOCTOR})
    public ResponseEntity<Map<String, Object>> getMedicalTypeByCode(@PathVariable String code) {
        MedicalTypeResponse response = medicalTypeService.getMedicalTypeByCode(code);
        return ResponseEntity.ok(Map.of(
                "message", "진료 유형 조회 성공",
                "data", response
        ));
    }

    @GetMapping("/active")
    @AuthRole({RoleType.STAFF, RoleType.ADMIN, RoleType.DOCTOR})
    public ResponseEntity<Map<String, Object>> getActiveMedicalTypes() {
        List<MedicalTypeResponse> medicalTypes = medicalTypeService.getActiveMedicalTypes();
        return ResponseEntity.ok(Map.of(
                "message", "활성화된 진료 유형 조회 성공",
                "data", medicalTypes
        ));
    }

    @GetMapping("/search")
    @AuthRole({RoleType.STAFF, RoleType.ADMIN, RoleType.DOCTOR})
    public ResponseEntity<Map<String, Object>> searchMedicalTypes(@RequestParam String keyword) {
        List<MedicalTypeResponse> medicalTypes = medicalTypeService.searchMedicalTypes(keyword);
        return ResponseEntity.ok(Map.of(
                "message", "진료 유형 검색 성공",
                "data", medicalTypes
        ));
    }

    @PutMapping("/{medicalTypeId}")
    @AuthRole({RoleType.ADMIN})
    public ResponseEntity<Map<String, Object>> updateMedicalType(
            @PathVariable Long medicalTypeId,
            @Valid @RequestBody MedicalTypeUpdateRequest request) {
        MedicalTypeResponse response = medicalTypeService.updateMedicalType(medicalTypeId, request);
        return ResponseEntity.ok(Map.of(
                "message", "진료 유형 수정 성공",
                "data", response
        ));
    }

    @DeleteMapping("/{medicalTypeId}")
    @AuthRole({RoleType.ADMIN})
    public ResponseEntity<Map<String, Object>> deleteMedicalType(@PathVariable Long medicalTypeId) {
        medicalTypeService.deleteMedicalType(medicalTypeId);
        return ResponseEntity.ok(Map.of(
                "message", "진료 유형 삭제 성공"
        ));
    }

    @PostMapping("/{medicalTypeId}/activate")
    @AuthRole({RoleType.ADMIN})
    public ResponseEntity<Map<String, Object>> activateMedicalType(@PathVariable Long medicalTypeId) {
        medicalTypeService.activateMedicalType(medicalTypeId);
        return ResponseEntity.ok(Map.of(
                "message", "진료 유형 활성화 성공"
        ));
    }

    @PostMapping("/{medicalTypeId}/deactivate")
    @AuthRole({RoleType.ADMIN})
    public ResponseEntity<Map<String, Object>> deactivateMedicalType(@PathVariable Long medicalTypeId) {
        medicalTypeService.deactivateMedicalType(medicalTypeId);
        return ResponseEntity.ok(Map.of(
                "message", "진료 유형 비활성화 성공"
        ));
    }
}

