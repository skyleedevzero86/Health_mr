package com.sleekydz86.support.doctortreatment.controller;

import com.sleekydz86.support.doctortreatment.dto.DoctorTreatmentRegisterRequest;
import com.sleekydz86.support.doctortreatment.dto.DoctorTreatmentResponse;
import com.sleekydz86.support.doctortreatment.dto.DoctorTreatmentStatisticsResponse;
import com.sleekydz86.support.doctortreatment.dto.DoctorTreatmentUpdateRequest;
import com.sleekydz86.support.doctortreatment.service.DoctorTreatmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class DoctorTreatmentController {

    private final DoctorTreatmentService doctorTreatmentService;


    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerDoctorTreatment(
            @RequestBody DoctorTreatmentRegisterRequest request) {
        try {
            DoctorTreatmentResponse response = doctorTreatmentService.registerDoctorTreatment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "의사 진료 시간 등록 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "의사 진료 시간 등록 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{doctorTreatmentId}")
    public ResponseEntity<Map<String, Object>> getDoctorTreatment(
            @PathVariable Long doctorTreatmentId) {
        try {
            DoctorTreatmentResponse response = doctorTreatmentService.getDoctorTreatment(doctorTreatmentId);
            return ResponseEntity.ok(Map.of(
                    "message", "의사 진료 정보 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "의사 진료 정보 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/doctor/{userId}")
    public ResponseEntity<Map<String, Object>> getDoctorTreatmentsByDoctor(
            @PathVariable Long userId) {
        try {
            List<DoctorTreatmentResponse> responses = doctorTreatmentService.getDoctorTreatmentsByDoctor(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "의사별 진료 목록 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "의사별 진료 목록 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/patient/{patientNo}")
    public ResponseEntity<Map<String, Object>> getDoctorTreatmentsByPatient(
            @PathVariable Long patientNo) {
        try {
            List<DoctorTreatmentResponse> responses = doctorTreatmentService.getDoctorTreatmentsByPatient(patientNo);
            return ResponseEntity.ok(Map.of(
                    "message", "환자별 진료 목록 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "환자별 진료 목록 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/doctor/{userId}/period")
    public ResponseEntity<Map<String, Object>> getDoctorTreatmentsByPeriod(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime end) {
        try {
            List<DoctorTreatmentResponse> responses = doctorTreatmentService
                    .getDoctorTreatmentsByPeriod(userId, start, end);
            return ResponseEntity.ok(Map.of(
                    "message", "의사별 기간별 진료 목록 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "의사별 기간별 진료 목록 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{doctorTreatmentId}")
    public ResponseEntity<Map<String, Object>> updateDoctorTreatment(
            @PathVariable Long doctorTreatmentId,
            @RequestBody DoctorTreatmentUpdateRequest request) {
        try {
            DoctorTreatmentResponse response = doctorTreatmentService
                    .updateDoctorTreatment(doctorTreatmentId, request);
            return ResponseEntity.ok(Map.of(
                    "message", "의사 진료 정보 수정 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "의사 진료 정보 수정 실패",
                    "error", e.getMessage()
            ));
        }
    }


    @DeleteMapping("/{doctorTreatmentId}")
    public ResponseEntity<Map<String, Object>> deleteDoctorTreatment(
            @PathVariable Long doctorTreatmentId) {
        try {
            doctorTreatmentService.deleteDoctorTreatment(doctorTreatmentId);
            return ResponseEntity.ok(Map.of(
                    "message", "의사 진료 정보 삭제 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "의사 진료 정보 삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }


    @GetMapping("/doctor/{userId}/statistics")
    public ResponseEntity<Map<String, Object>> getDoctorTreatmentStatistics(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime end) {
        try {
            DoctorTreatmentStatisticsResponse response = doctorTreatmentService
                    .getDoctorTreatmentStatistics(userId, start, end);
            return ResponseEntity.ok(Map.of(
                    "message", "의사별 진료 통계 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "의사별 진료 통계 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

