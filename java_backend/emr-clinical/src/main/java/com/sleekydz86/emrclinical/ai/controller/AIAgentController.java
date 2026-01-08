package com.sleekydz86.emrclinical.ai.controller;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.emrclinical.ai.dto.*;
import com.sleekydz86.emrclinical.ai.service.AIAnalysisService;
import com.sleekydz86.emrclinical.ai.service.AIRecommendationService;
import com.sleekydz86.emrclinical.ai.service.AIReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIAgentController {

    private final AIAnalysisService aiAnalysisService;
    private final AIRecommendationService aiRecommendationService;
    private final AIReportService aiReportService;

    @PostMapping("/analysis/pattern")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.READ)
    public ResponseEntity<TreatmentPatternAnalysisResponse> analyzeTreatmentPatterns(
            @Valid @RequestBody TreatmentPatternAnalysisRequest request) {
        TreatmentPatternAnalysisResponse response = aiAnalysisService.analyzeTreatmentPatterns(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analysis/patient/{patientNo}")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.READ)
    public ResponseEntity<PatientHistoryAnalysisResponse> analyzePatientHistory(
            @PathVariable Long patientNo) {
        PatientHistoryAnalysisResponse response = aiAnalysisService.analyzePatientHistory(patientNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analysis/anomalies")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.READ)
    public ResponseEntity<AnomalyDetectionResponse> detectAnomalies() {
        AnomalyDetectionResponse response = aiAnalysisService.detectAnomalies();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recommendation/treatment")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.READ)
    public ResponseEntity<TreatmentRecommendationResponse> recommendTreatmentType(
            @Valid @RequestBody TreatmentRecommendationRequest request) {
        TreatmentRecommendationResponse response = aiRecommendationService.recommendTreatmentType(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recommendation/doctor")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.READ)
    public ResponseEntity<DoctorRecommendationResponse> recommendDoctor(
            @Valid @RequestBody DoctorRecommendationRequest request) {
        DoctorRecommendationResponse response = aiRecommendationService.recommendDoctor(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/report/generate")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.READ)
    public ResponseEntity<TreatmentReportResponse> generateTreatmentReport(
            @Valid @RequestBody TreatmentReportRequest request) {
        TreatmentReportResponse response = aiReportService.generateTreatmentReport(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/schedule/optimize")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.READ)
    public ResponseEntity<ScheduleOptimizationResponse> optimizeSchedule(
            @Valid @RequestBody ScheduleOptimizationRequest request) {
        ScheduleOptimizationResponse response = aiReportService.optimizeSchedule(request);
        return ResponseEntity.ok(response);
    }
}

