package com.sleekydz86.support.examination.journal.controller;

import com.sleekydz86.support.examination.journal.dto.ExaminationScheduleRegisterRequest;
import com.sleekydz86.support.examination.journal.dto.ExaminationScheduleResponse;
import com.sleekydz86.support.examination.journal.dto.ExaminationScheduleUpdateRequest;
import com.sleekydz86.support.examination.journal.service.ExaminationScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examination/schedule")
@RequiredArgsConstructor
public class ExaminationScheduleController {

    private final ExaminationScheduleService scheduleService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerSchedule(@RequestBody ExaminationScheduleRegisterRequest request) {
        try {
            ExaminationScheduleResponse response = scheduleService.registerSchedule(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "검사 일정이 등록되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "검사 일정 등록 실패",
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "검사 일정 등록 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<Map<String, Object>> getSchedule(@PathVariable Long scheduleId) {
        try {
            ExaminationScheduleResponse response = scheduleService.getSchedule(scheduleId);
            return ResponseEntity.ok(Map.of(
                    "message", "검사 일정 조회 성공",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "검사 일정 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/patient/{patientNo}")
    public ResponseEntity<Map<String, Object>> getSchedulesByPatient(@PathVariable Long patientNo) {
        try {
            List<ExaminationScheduleResponse> responses = scheduleService.getSchedulesByPatient(patientNo);
            return ResponseEntity.ok(Map.of(
                    "message", "환자별 검사 일정 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "환자별 검사 일정 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/examination/{examinationId}")
    public ResponseEntity<Map<String, Object>> getSchedulesByExamination(@PathVariable Long examinationId) {
        try {
            List<ExaminationScheduleResponse> responses = scheduleService.getSchedulesByExamination(examinationId);
            return ResponseEntity.ok(Map.of(
                    "message", "검사별 검사 일정 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "검사별 검사 일정 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<Map<String, Object>> getSchedulesByDate(@PathVariable LocalDate date) {
        try {
            List<ExaminationScheduleResponse> responses = scheduleService.getSchedulesByDate(date);
            return ResponseEntity.ok(Map.of(
                    "message", "날짜별 검사 일정 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "날짜별 검사 일정 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<Map<String, Object>> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ExaminationScheduleUpdateRequest request) {
        try {
            ExaminationScheduleResponse response = scheduleService.updateSchedule(scheduleId, request);
            return ResponseEntity.ok(Map.of(
                    "message", "검사 일정이 수정되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "검사 일정 수정 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Map<String, Object>> deleteSchedule(@PathVariable Long scheduleId) {
        try {
            ExaminationScheduleResponse response = scheduleService.deleteSchedule(scheduleId);
            return ResponseEntity.ok(Map.of(
                    "message", "검사 일정이 삭제되었습니다.",
                    "deletedSchedule", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "검사 일정 삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

