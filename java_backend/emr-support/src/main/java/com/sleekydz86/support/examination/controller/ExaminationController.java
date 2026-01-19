package com.sleekydz86.support.examination.controller;

import com.sleekydz86.support.examination.dto.ExaminationRegisterRequest;
import com.sleekydz86.support.examination.dto.ExaminationResponse;
import com.sleekydz86.support.examination.dto.ExaminationUpdateRequest;
import com.sleekydz86.support.examination.entity.ExaminationEntity;
import com.sleekydz86.support.examination.service.ExaminationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examination")
@RequiredArgsConstructor
public class ExaminationController {

    private final ExaminationService examinationService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerExamination(@RequestBody ExaminationRegisterRequest request) {
        ExaminationEntity responseData = examinationService.registerExamination(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "등록 성공",
                "data", responseData
        ));
    }

    @GetMapping("/read/{examinationId}")
    public ResponseEntity<Map<String, Object>> viewExamination(@PathVariable Long examinationId) {
        ExaminationResponse examinationResponse = examinationService.readExamination(examinationId);
        return ResponseEntity.ok(Map.of(
                "message", "조회 성공",
                "data", examinationResponse
        ));
    }

    @GetMapping("/read/equipment/{equipmentId}")
    public ResponseEntity<Map<String, Object>> getExaminationByEquipmentId(@PathVariable("equipmentId") Long equipmentId) {
        List<ExaminationResponse> examinationResponses = examinationService.readExaminationByEquipmentId(equipmentId);
        return ResponseEntity.ok(Map.of(
                "message", "장비별 조회 성공",
                "data", examinationResponses
        ));
    }

    @GetMapping("/read/all")
    public ResponseEntity<Map<String, Object>> getAllExaminationInfo() {
        List<ExaminationResponse> examinationResponses = examinationService.readAllExamination();
        return ResponseEntity.ok(Map.of(
                "message", "전체 조회 성공",
                "data", examinationResponses
        ));
    }

    @PostMapping("/update/{examinationId}")
    public ResponseEntity<Map<String, Object>> updateExamination(
            @PathVariable Long examinationId,
            @RequestBody ExaminationUpdateRequest request) {
        ExaminationEntity updatedData = examinationService.updateExamination(examinationId, request);
        return ResponseEntity.ok(Map.of(
                "message", "수정 성공",
                "data", updatedData
        ));
    }

    @PostMapping("/delete/{examinationId}")
    public ResponseEntity<Map<String, Object>> deleteExamination(@PathVariable Long examinationId) {
        ExaminationResponse deletedExamination = examinationService.deleteExamination(examinationId);
        return ResponseEntity.ok(Map.of(
                "message", "삭제 성공",
                "deletedExamination", deletedExamination
        ));
    }
}

