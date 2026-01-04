package com.sleekydz86.support.examination.journal.controller;

import com.sleekydz86.support.examination.journal.dto.ExaminationResultRegisterRequest;
import com.sleekydz86.support.examination.journal.dto.ExaminationResultResponse;
import com.sleekydz86.support.examination.journal.dto.ExaminationResultUpdateRequest;
import com.sleekydz86.support.examination.journal.service.ExaminationResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examination/result")
@RequiredArgsConstructor
public class ExaminationResultController {

    private final ExaminationResultService resultService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerResult(@RequestBody ExaminationResultRegisterRequest request) {
        try {
            ExaminationResultResponse response = resultService.registerResult(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "검사 결과가 등록되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "검사 결과 등록 실패",
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "검사 결과 등록 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{resultId}")
    public ResponseEntity<Map<String, Object>> getResult(@PathVariable Long resultId) {
        try {
            ExaminationResultResponse response = resultService.getResult(resultId);
            return ResponseEntity.ok(Map.of(
                    "message", "검사 결과 조회 성공",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "검사 결과 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/patient/{patientNo}")
    public ResponseEntity<Map<String, Object>> getResultsByPatient(@PathVariable Long patientNo) {
        try {
            List<ExaminationResultResponse> responses = resultService.getResultsByPatient(patientNo);
            return ResponseEntity.ok(Map.of(
                    "message", "환자별 검사 결과 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "환자별 검사 결과 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/examination/{examinationId}")
    public ResponseEntity<Map<String, Object>> getResultsByExamination(@PathVariable Long examinationId) {
        try {
            List<ExaminationResultResponse> responses = resultService.getResultsByExamination(examinationId);
            return ResponseEntity.ok(Map.of(
                    "message", "검사별 검사 결과 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "검사별 검사 결과 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<Map<String, Object>> getResultsByDate(@PathVariable LocalDate date) {
        try {
            List<ExaminationResultResponse> responses = resultService.getResultsByDate(date);
            return ResponseEntity.ok(Map.of(
                    "message", "날짜별 검사 결과 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "날짜별 검사 결과 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{resultId}")
    public ResponseEntity<Map<String, Object>> updateResult(
            @PathVariable Long resultId,
            @RequestBody ExaminationResultUpdateRequest request) {
        try {
            ExaminationResultResponse response = resultService.updateResult(resultId, request);
            return ResponseEntity.ok(Map.of(
                    "message", "검사 결과가 수정되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "검사 결과 수정 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{resultId}")
    public ResponseEntity<Map<String, Object>> deleteResult(@PathVariable Long resultId) {
        try {
            ExaminationResultResponse response = resultService.deleteResult(resultId);
            return ResponseEntity.ok(Map.of(
                    "message", "검사 결과가 삭제되었습니다.",
                    "deletedResult", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "검사 결과 삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

