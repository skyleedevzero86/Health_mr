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
        try {
            DisabilityEntity responseData = disabilityService.registerDisability(request);
            
            String patientName = responseData.getPatientEntity() != null 
                    ? responseData.getPatientEntity().getPatientName() 
                    : "Unknown";

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "등록 성공",
                    "patientName", patientName,
                    "data", responseData
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "등록 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/read/{patientNo}")
    public ResponseEntity<Map<String, Object>> viewDisability(@PathVariable Long patientNo) {
        try {
            DisabilityResponse disabilityResponse = disabilityService.readDisabilityByPatientNo(patientNo);

            if (disabilityResponse == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "message", "장애인 정보를 찾을 수 없습니다."
                ));
            }

            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "조회 성공",
                    "data", disabilityResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/read/all")
    public ResponseEntity<Map<String, Object>> getAllDisabilityInfo() {
        try {
            List<DisabilityResponse> disabilityResponses = disabilityService.readAllDisabilities();
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "전체 조회 성공",
                    "data", disabilityResponses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "전체 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/update/{patientNo}")
    public ResponseEntity<Map<String, Object>> updateDisability(
            @PathVariable Long patientNo,
            @RequestBody DisabilityUpdateRequest request) {
        try {
            DisabilityEntity updatedData = disabilityService.updateDisability(patientNo, request);

            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "수정 성공",
                    "data", updatedData
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "수정 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/delete/{patientNo}")
    public ResponseEntity<Map<String, Object>> deleteDisability(@PathVariable Long patientNo) {
        try {
            DisabilityResponse deletedDisability = disabilityService.deleteDisability(patientNo);

            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "삭제 성공",
                    "deletedDisability", deletedDisability
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{patientNo}/recommendations")
    @AuthRole(roles = {"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getDisabilityWithRecommendations(
            @PathVariable Long patientNo) {
        try {
            DisabilityWithCareInstitutionResponse response =
                disabilityService.getDisabilityWithRecommendations(patientNo);

            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "장애인 정보를 찾을 수 없습니다."
                ));
            }

            return ResponseEntity.ok(Map.of(
                "message", "조회 성공",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "message", "조회 실패",
                "error", e.getMessage()
            ));
        }
    }
}

