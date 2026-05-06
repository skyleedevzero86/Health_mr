package com.sleekydz86.support.disability.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.support.disability.dto.DisabilityRegisterRequest;
import com.sleekydz86.support.disability.dto.DisabilityResponse;
import com.sleekydz86.support.disability.dto.DisabilityUpdateRequest;
import com.sleekydz86.support.disability.dto.DisabilityWithCareInstitutionResponse;
import com.sleekydz86.support.disability.entity.DisabilityEntity;
import com.sleekydz86.support.disability.service.DisabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disability")
@RequiredArgsConstructor
public class DisabilityController {

    private final DisabilityService disabilityService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerDisability(
            @RequestBody DisabilityRegisterRequest request) {
        DisabilityEntity responseData = disabilityService.registerDisability(request);
        
        String patientName = responseData.getPatientEntity() != null 
                ? responseData.getPatientEntity().getPatientName() 
                : "Unknown";

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "등록 성공",
                "patientName", patientName,
                "data", responseData
        ));
    }

    @GetMapping("/read/{patientNo}")
    public ResponseEntity<Map<String, Object>> viewDisability(@PathVariable Long patientNo) {
        DisabilityResponse disabilityResponse = disabilityService.readDisabilityByPatientNo(patientNo);
        return ResponseEntity.ok(Map.of(
                "message", "조회 성공",
                "data", disabilityResponse
        ));
    }

    @GetMapping("/read/all")
    public ResponseEntity<Map<String, Object>> getAllDisabilityInfo() {
        List<DisabilityResponse> disabilityResponses = disabilityService.readAllDisabilities();
        return ResponseEntity.ok(Map.of(
                "message", "전체 조회 성공",
                "data", disabilityResponses
        ));
    }

    @PostMapping("/update/{patientNo}")
    public ResponseEntity<Map<String, Object>> updateDisability(
            @PathVariable Long patientNo,
            @RequestBody DisabilityUpdateRequest request) {
        DisabilityEntity updatedData = disabilityService.updateDisability(patientNo, request);
        return ResponseEntity.ok(Map.of(
                "message", "수정 성공",
                "data", updatedData
        ));
    }

    @PostMapping("/delete/{patientNo}")
    public ResponseEntity<Map<String, Object>> deleteDisability(@PathVariable Long patientNo) {
        DisabilityResponse deletedDisability = disabilityService.deleteDisability(patientNo);
        return ResponseEntity.ok(Map.of(
                "message", "삭제 성공",
                "deletedDisability", deletedDisability
        ));
    }

    @GetMapping("/{patientNo}/recommendations")
    @AuthRole(roles = {"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getDisabilityWithRecommendations(
            @PathVariable Long patientNo) {
        DisabilityWithCareInstitutionResponse response =
            disabilityService.getDisabilityWithRecommendations(patientNo);
        return ResponseEntity.ok(Map.of(
            "message", "조회 성공",
            "data", response
        ));
    }
}

