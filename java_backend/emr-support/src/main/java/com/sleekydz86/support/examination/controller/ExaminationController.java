package com.sleekydz86.support.examination.controller;

import com.sleekydz86.support.examination.dto.ExaminationRegisterRequest;
import com.sleekydz86.support.examination.dto.ExaminationResponse;
import com.sleekydz86.support.examination.dto.ExaminationUpdateRequest;
import com.sleekydz86.support.examination.entity.ExaminationEntity;
import com.sleekydz86.support.examination.service.ExaminationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examination")
public class ExaminationController {

    private final ExaminationService examinationService;

    public ExaminationController(ExaminationService examinationService) {
        this.examinationService = examinationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerExamination(@RequestBody ExaminationRegisterRequest request) {
        try {
            ExaminationEntity responseData = examinationService.registerExamination(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "등록 성공",
                    "data", responseData
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "등록 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/read/{examinationId}")
    public ResponseEntity<Map<String, Object>> viewExamination(@PathVariable Long examinationId) {
        try {
            ExaminationResponse examinationResponse = examinationService.readExamination(examinationId);
            if (examinationResponse == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "검사 정보를 찾을 수 없습니다."));
            }

            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "조회 성공",
                    "data", examinationResponse
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/read/equipment/{equipmentId}")
    public ResponseEntity<Map<String, Object>> getExaminationByEquipmentId(@PathVariable("equipmentId") Long equipmentId) {
        try {
            List<ExaminationResponse> examinationResponses = examinationService.readExaminationByEquipmentId(equipmentId);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "장비별 조회 성공",
                    "data", examinationResponses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "장비별 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/read/all")
    public ResponseEntity<Map<String, Object>> getAllExaminationInfo() {
        try {
            List<ExaminationResponse> examinationResponses = examinationService.readAllExamination();
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "전체 조회 성공",
                    "data", examinationResponses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "전체 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/update/{examinationId}")
    public ResponseEntity<Map<String, Object>> updateExamination(
            @PathVariable Long examinationId,
            @RequestBody ExaminationUpdateRequest request) {

        try {
            ExaminationEntity updatedData = examinationService.updateExamination(examinationId, request);

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

    @PostMapping("/delete/{examinationId}")
    public ResponseEntity<Map<String, Object>> deleteExamination(@PathVariable Long examinationId) {
        try {
            ExaminationResponse deletedExamination = examinationService.deleteExamination(examinationId);

            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "삭제 성공",
                    "deletedExamination", deletedExamination
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

