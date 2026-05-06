package com.sleekydz86.emrclinical.ai.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.emrclinical.ai.dto.AnomalyDetectionResponse;
import com.sleekydz86.emrclinical.ai.dto.DoctorRecommendationRequest;
import com.sleekydz86.emrclinical.ai.dto.DoctorRecommendationResponse;
import com.sleekydz86.emrclinical.ai.dto.PatientHistoryAnalysisResponse;
import com.sleekydz86.emrclinical.ai.dto.ScheduleOptimizationRequest;
import com.sleekydz86.emrclinical.ai.dto.ScheduleOptimizationResponse;
import com.sleekydz86.emrclinical.ai.dto.TreatmentPatternAnalysisRequest;
import com.sleekydz86.emrclinical.ai.dto.TreatmentPatternAnalysisResponse;
import com.sleekydz86.emrclinical.ai.dto.TreatmentRecommendationRequest;
import com.sleekydz86.emrclinical.ai.dto.TreatmentRecommendationResponse;
import com.sleekydz86.emrclinical.ai.dto.TreatmentReportRequest;
import com.sleekydz86.emrclinical.ai.dto.TreatmentReportResponse;
import com.sleekydz86.emrclinical.ai.service.AIAnalysisService;
import com.sleekydz86.emrclinical.ai.service.AIRecommendationService;
import com.sleekydz86.emrclinical.ai.service.AIReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIAgentController {

    private final AIAnalysisService aiAnalysisService;
    private final AIRecommendationService aiRecommendationService;
    private final AIReportService aiReportService;

    @PostMapping("/analysis/pattern")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<TreatmentPatternAnalysisResponse> analyzeTreatmentPatterns(
            @AuthUser Long userId,
            @Valid @RequestBody TreatmentPatternAnalysisRequest request) {
        return ResponseEntity.ok(aiAnalysisService.analyzeTreatmentPatterns(userId, request));
    }

    @GetMapping("/analysis/patient/{patientNo}")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<PatientHistoryAnalysisResponse> analyzePatientHistory(
            @AuthUser Long userId,
            @PathVariable Long patientNo) {
        return ResponseEntity.ok(aiAnalysisService.analyzePatientHistory(userId, patientNo));
    }

    @GetMapping("/analysis/anomalies")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<AnomalyDetectionResponse> detectAnomalies(@AuthUser Long userId) {
        return ResponseEntity.ok(aiAnalysisService.detectAnomalies(userId));
    }

    @PostMapping("/recommendation/treatment")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<TreatmentRecommendationResponse> recommendTreatmentType(
            @AuthUser Long userId,
            @Valid @RequestBody TreatmentRecommendationRequest request) {
        return ResponseEntity.ok(aiRecommendationService.recommendTreatmentType(userId, request));
    }

    @PostMapping("/recommendation/doctor")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<DoctorRecommendationResponse> recommendDoctor(
            @AuthUser Long userId,
            @Valid @RequestBody DoctorRecommendationRequest request) {
        return ResponseEntity.ok(aiRecommendationService.recommendDoctor(userId, request));
    }

    @PostMapping("/report/generate")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<TreatmentReportResponse> generateTreatmentReport(
            @AuthUser Long userId,
            @Valid @RequestBody TreatmentReportRequest request) {
        return ResponseEntity.ok(aiReportService.generateTreatmentReport(userId, request));
    }

    @PostMapping("/schedule/optimize")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<ScheduleOptimizationResponse> optimizeSchedule(
            @AuthUser Long userId,
            @Valid @RequestBody ScheduleOptimizationRequest request) {
        return ResponseEntity.ok(aiReportService.optimizeSchedule(userId, request));
    }
}
