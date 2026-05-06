package com.sleekydz86.emrclinical.ai.service;

import com.sleekydz86.core.audit.service.AuditService;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.emrclinical.ai.dto.AnomalyDetectionResponse;
import com.sleekydz86.emrclinical.ai.dto.PatientHistoryAnalysisResponse;
import com.sleekydz86.emrclinical.ai.dto.TreatmentPatternAnalysisRequest;
import com.sleekydz86.emrclinical.ai.dto.TreatmentPatternAnalysisResponse;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.checkin.repository.CheckInRepository;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.emrclinical.types.TreatmentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AIAnalysisService {

    private final TreatmentRepository treatmentRepository;
    private final CheckInRepository checkInRepository;
    private final PatientService patientService;
    private final AuditService auditService;

    public TreatmentPatternAnalysisResponse analyzeTreatmentPatterns(Long userId, TreatmentPatternAnalysisRequest request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

        List<TreatmentEntity> treatments = treatmentRepository.findByConditions(
                null,
                request.getDoctorId(),
                request.getDepartmentId(),
                null,
                null,
                startDateTime,
                endDateTime
        );

        Map<String, Integer> typeDistribution = toCountMap(
                treatments.stream()
                        .map(treatment -> treatment.getTreatmentType() != null
                                ? treatment.getTreatmentType().name()
                                : "UNKNOWN")
                        .toList());

        Map<String, Integer> departmentDistribution = toCountMap(
                treatments.stream()
                        .filter(treatment -> treatment.getTreatmentDept() != null)
                        .map(TreatmentEntity::getTreatmentDept)
                        .toList());

        Map<String, Integer> doctorDistribution = toCountMap(
                treatments.stream()
                        .filter(treatment -> treatment.getTreatmentDoc() != null)
                        .map(treatment -> treatment.getTreatmentDoc().getName())
                        .toList());

        Map<String, Integer> dailyTrend = toCountMap(
                treatments.stream()
                        .map(treatment -> treatment.getTreatmentDate().toLocalDate().toString())
                        .toList());

        List<TreatmentPatternAnalysisResponse.Insight> insights =
                generatePatternInsights(typeDistribution, departmentDistribution, doctorDistribution, treatments.size());

        long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        TreatmentPatternAnalysisResponse response = TreatmentPatternAnalysisResponse.builder()
                .period(daysBetween + "일")
                .totalTreatments(treatments.size())
                .typeDistribution(typeDistribution)
                .departmentDistribution(departmentDistribution)
                .doctorDistribution(doctorDistribution)
                .dailyTrend(dailyTrend)
                .insights(insights)
                .build();

        Map<String, Object> sourceData = new LinkedHashMap<>();
        sourceData.put("sourceDatabase", "MYSQL");
        sourceData.put("startDateTime", startDateTime);
        sourceData.put("endDateTime", endDateTime);
        sourceData.put("doctorId", request.getDoctorId());
        sourceData.put("departmentId", request.getDepartmentId());
        sourceData.put("treatmentCount", treatments.size());

        Map<String, Object> responseSummary = new LinkedHashMap<>();
        responseSummary.put("typeBucketCount", typeDistribution.size());
        responseSummary.put("departmentBucketCount", departmentDistribution.size());
        responseSummary.put("doctorBucketCount", doctorDistribution.size());
        responseSummary.put("insightCount", insights.size());

        auditUsage(userId, "TREATMENT_PATTERN_ANALYSIS", request, sourceData, responseSummary);
        return response;
    }

    public PatientHistoryAnalysisResponse analyzePatientHistory(Long userId, Long patientNo) {
        return analyzePatientHistoryInternal(patientNo, userId, true);
    }

    PatientHistoryAnalysisResponse analyzePatientHistoryInternal(Long patientNo) {
        return analyzePatientHistoryInternal(patientNo, null, false);
    }

    public AnomalyDetectionResponse detectAnomalies(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<TreatmentEntity> treatments = treatmentRepository.findByConditions(
                null,
                null,
                null,
                null,
                null,
                startDateTime,
                endDateTime
        );

        List<AnomalyDetectionResponse.Anomaly> anomalies = new ArrayList<>();
        if (!treatments.isEmpty()) {
            double averagePerDay = (double) treatments.size() / 7.0;
            Map<LocalDate, Integer> dailyCount = new LinkedHashMap<>();
            for (TreatmentEntity treatment : treatments) {
                LocalDate treatmentDate = treatment.getTreatmentDate().toLocalDate();
                dailyCount.merge(treatmentDate, 1, Integer::sum);
            }

            dailyCount.forEach((date, count) -> {
                if (count > averagePerDay * 2) {
                    anomalies.add(AnomalyDetectionResponse.Anomaly.builder()
                            .type("spike")
                            .date(date)
                            .count(count)
                            .message(String.format("%s 진료 건수가 평균 %.1f건 대비 급증했습니다.", date, averagePerDay))
                            .build());
                }
            });

            long emergencyCount = treatments.stream()
                    .filter(treatment -> treatment.getTreatmentType() == TreatmentType.EMERGENCY)
                    .count();
            double emergencyRatio = (double) emergencyCount / treatments.size();
            if (emergencyRatio > 0.5 && treatments.size() > 10) {
                anomalies.add(AnomalyDetectionResponse.Anomaly.builder()
                        .type("ratio")
                        .message(String.format("응급 진료 비율이 %.1f%%로 높게 탐지되었습니다.", emergencyRatio * 100))
                        .build());
            }
        }

        AnomalyDetectionResponse response = AnomalyDetectionResponse.builder()
                .anomalies(anomalies)
                .totalDetected(anomalies.size())
                .build();

        Map<String, Object> sourceData = new LinkedHashMap<>();
        sourceData.put("sourceDatabase", "MYSQL");
        sourceData.put("startDate", startDate);
        sourceData.put("endDate", endDate);
        sourceData.put("treatmentCount", treatments.size());
        sourceData.put("emergencyCount", treatments.stream()
                .filter(treatment -> treatment.getTreatmentType() == TreatmentType.EMERGENCY)
                .count());

        Map<String, Object> responseSummary = new LinkedHashMap<>();
        responseSummary.put("anomalyCount", anomalies.size());

        auditUsage(userId, "ANOMALY_DETECTION", Map.of("windowDays", 7), sourceData, responseSummary);
        return response;
    }

    private PatientHistoryAnalysisResponse analyzePatientHistoryInternal(Long patientNo, Long userId, boolean auditEnabled) {
        PatientEntity patient = patientService.getPatientByNo(patientNo);
        List<CheckInEntity> checkIns = checkInRepository.findByConditions(patientNo, null, null, null, null);
        List<Long> checkInIds = checkIns.stream()
                .map(CheckInEntity::getCheckInId)
                .toList();

        List<TreatmentEntity> treatments = checkInIds.isEmpty()
                ? Collections.emptyList()
                : treatmentRepository.findByCheckInIds(checkInIds);

        PatientHistoryAnalysisResponse response;
        if (treatments.isEmpty()) {
            response = PatientHistoryAnalysisResponse.builder()
                    .patientNo(patientNo)
                    .patientName(patient.getPatientName())
                    .totalTreatments(0)
                    .typeDistribution(Collections.emptyMap())
                    .departmentDistribution(Collections.emptyMap())
                    .doctorDistribution(Collections.emptyMap())
                    .revisitPattern(PatientHistoryAnalysisResponse.RevisitPattern.builder()
                            .frequency("low")
                            .averageDays(null)
                            .intervals(Collections.emptyList())
                            .build())
                    .insights(Collections.emptyList())
                    .build();
        } else {
            Map<String, Integer> typeDistribution = toCountMap(
                    treatments.stream()
                            .map(treatment -> treatment.getTreatmentType() != null
                                    ? treatment.getTreatmentType().name()
                                    : "UNKNOWN")
                            .toList());

            Map<String, Integer> departmentDistribution = toCountMap(
                    treatments.stream()
                            .filter(treatment -> treatment.getTreatmentDept() != null)
                            .map(TreatmentEntity::getTreatmentDept)
                            .toList());

            Map<String, Integer> doctorDistribution = toCountMap(
                    treatments.stream()
                            .filter(treatment -> treatment.getTreatmentDoc() != null)
                            .map(treatment -> treatment.getTreatmentDoc().getName())
                            .toList());

            List<LocalDate> dates = treatments.stream()
                    .map(treatment -> treatment.getTreatmentDate().toLocalDate())
                    .sorted()
                    .toList();

            PatientHistoryAnalysisResponse.RevisitPattern revisitPattern = analyzeRevisitPattern(dates);
            List<PatientHistoryAnalysisResponse.Insight> insights = generatePatientInsights(treatments, revisitPattern);

            response = PatientHistoryAnalysisResponse.builder()
                    .patientNo(patientNo)
                    .patientName(patient.getPatientName())
                    .totalTreatments(treatments.size())
                    .firstVisit(dates.get(0))
                    .lastVisit(dates.get(dates.size() - 1))
                    .typeDistribution(typeDistribution)
                    .departmentDistribution(departmentDistribution)
                    .doctorDistribution(doctorDistribution)
                    .revisitPattern(revisitPattern)
                    .insights(insights)
                    .build();
        }

        if (auditEnabled) {
            Map<String, Object> sourceData = new LinkedHashMap<>();
            sourceData.put("sourceDatabase", "MYSQL");
            sourceData.put("patientNo", patientNo);
            sourceData.put("patientName", patient.getPatientName());
            sourceData.put("checkInCount", checkIns.size());
            sourceData.put("treatmentCount", treatments.size());

            Map<String, Object> responseSummary = new LinkedHashMap<>();
            responseSummary.put("firstVisit", response.getFirstVisit());
            responseSummary.put("lastVisit", response.getLastVisit());
            responseSummary.put("revisitFrequency",
                    response.getRevisitPattern() != null ? response.getRevisitPattern().getFrequency() : null);
            responseSummary.put("insightCount", response.getInsights() != null ? response.getInsights().size() : 0);

            auditUsage(userId, "PATIENT_HISTORY_ANALYSIS", Map.of("patientNo", patientNo), sourceData, responseSummary);
        }

        return response;
    }

    private Map<String, Integer> toCountMap(List<String> values) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String value : values) {
            counts.merge(value, 1, Integer::sum);
        }
        return counts;
    }

    private List<TreatmentPatternAnalysisResponse.Insight> generatePatternInsights(
            Map<String, Integer> typeDistribution,
            Map<String, Integer> departmentDistribution,
            Map<String, Integer> doctorDistribution,
            int totalTreatments) {
        List<TreatmentPatternAnalysisResponse.Insight> insights = new ArrayList<>();

        addTopDistributionInsight(insights, "info", "가장 많은 진료 유형", typeDistribution, totalTreatments);
        addTopDistributionInsight(insights, "warning", "가장 많은 진료과", departmentDistribution, totalTreatments);

        if (!doctorDistribution.isEmpty()) {
            Map.Entry<String, Integer> topDoctor = doctorDistribution.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            if (topDoctor != null && totalTreatments > 0) {
                double ratio = (double) topDoctor.getValue() / totalTreatments;
                if (ratio >= 0.3) {
                    insights.add(TreatmentPatternAnalysisResponse.Insight.builder()
                            .type("warning")
                            .title("진료 담당 집중도")
                            .message(String.format("%s 의사가 전체 진료의 %.1f%%를 담당했습니다.", topDoctor.getKey(), ratio * 100))
                            .build());
                }
            }
        }

        return insights;
    }

    private void addTopDistributionInsight(
            List<TreatmentPatternAnalysisResponse.Insight> insights,
            String type,
            String title,
            Map<String, Integer> distribution,
            int totalTreatments) {
        if (distribution.isEmpty() || totalTreatments == 0) {
            return;
        }

        Map.Entry<String, Integer> topEntry = distribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        if (topEntry == null) {
            return;
        }

        double percentage = (double) topEntry.getValue() / totalTreatments * 100;
        insights.add(TreatmentPatternAnalysisResponse.Insight.builder()
                .type(type)
                .title(title)
                .message(String.format("%s 비중이 %.1f%%입니다.", topEntry.getKey(), percentage))
                .build());
    }

    private PatientHistoryAnalysisResponse.RevisitPattern analyzeRevisitPattern(List<LocalDate> dates) {
        if (dates.size() < 2) {
            return PatientHistoryAnalysisResponse.RevisitPattern.builder()
                    .frequency("low")
                    .averageDays(null)
                    .intervals(Collections.emptyList())
                    .build();
        }

        List<Double> intervals = new ArrayList<>();
        for (int i = 1; i < dates.size(); i++) {
            intervals.add((double) ChronoUnit.DAYS.between(dates.get(i - 1), dates.get(i)));
        }

        double averageInterval = intervals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        String frequency;
        if (averageInterval < 30) {
            frequency = "high";
        } else if (averageInterval < 90) {
            frequency = "medium";
        } else {
            frequency = "low";
        }

        return PatientHistoryAnalysisResponse.RevisitPattern.builder()
                .frequency(frequency)
                .averageDays((int) Math.round(averageInterval))
                .intervals(intervals)
                .build();
    }

    private List<PatientHistoryAnalysisResponse.Insight> generatePatientInsights(
            List<TreatmentEntity> treatments,
            PatientHistoryAnalysisResponse.RevisitPattern revisitPattern) {
        List<PatientHistoryAnalysisResponse.Insight> insights = new ArrayList<>();

        if ("high".equals(revisitPattern.getFrequency())) {
            insights.add(PatientHistoryAnalysisResponse.Insight.builder()
                    .type("info")
                    .message("재방문 주기가 짧아 지속 관찰이 필요한 환자 패턴으로 보입니다.")
                    .build());
        }

        long emergencyCount = treatments.stream()
                .filter(treatment -> treatment.getTreatmentType() == TreatmentType.EMERGENCY)
                .count();
        if (emergencyCount >= 2) {
            insights.add(PatientHistoryAnalysisResponse.Insight.builder()
                    .type("warning")
                    .message("응급 진료 이력이 반복되어 추가 모니터링이 필요합니다.")
                    .build());
        }

        return insights;
    }

    private void auditUsage(Long userId, String entityId, Object requestData, Object sourceData, Object responseSummary) {
        if (userId == null) {
            return;
        }

        Map<String, Object> beforeData = new LinkedHashMap<>();
        beforeData.put("request", requestData);
        beforeData.put("sourceData", sourceData);

        Map<String, Object> afterData = new LinkedHashMap<>();
        afterData.put("responseSummary", responseSummary);

        auditService.logAudit(userId, "AI_USAGE", "AI", entityId, beforeData, afterData, null, null);
    }
}
