package com.sleekydz86.emrclinical.ai.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AIAnalysisService {

    private final TreatmentRepository treatmentRepository;
    private final CheckInRepository checkInRepository;
    private final PatientService patientService;

    public TreatmentPatternAnalysisResponse analyzeTreatmentPatterns(TreatmentPatternAnalysisRequest request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

        List<TreatmentEntity> treatments = treatmentRepository.findByTreatmentDateBetween(startDateTime, endDateTime);

        if (request.getDepartmentId() != null) {
            treatments = treatments.stream()
                    .filter(t -> t.getDepartmentEntity() != null && 
                            t.getDepartmentEntity().getDepartmentId().equals(request.getDepartmentId()))
                    .collect(Collectors.toList());
        }

        if (request.getDoctorId() != null) {
            treatments = treatments.stream()
                    .filter(t -> t.getTreatmentDoc() != null && 
                            t.getTreatmentDoc().getId().equals(request.getDoctorId()))
                    .collect(Collectors.toList());
        }

        Map<String, Integer> typeDistribution = treatments.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTreatmentType() != null ? t.getTreatmentType().name() : "UNKNOWN",
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        Map<String, Integer> departmentDistribution = treatments.stream()
                .filter(t -> t.getTreatmentDept() != null)
                .collect(Collectors.groupingBy(
                        TreatmentEntity::getTreatmentDept,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        Map<String, Integer> doctorDistribution = treatments.stream()
                .filter(t -> t.getTreatmentDoc() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getTreatmentDoc().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        Map<String, Integer> dailyTrend = treatments.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTreatmentDate().toLocalDate().toString(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        List<TreatmentPatternAnalysisResponse.Insight> insights = generateInsights(
                typeDistribution, departmentDistribution, doctorDistribution, treatments);

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        return TreatmentPatternAnalysisResponse.builder()
                .period(daysBetween + "일")
                .totalTreatments(treatments.size())
                .typeDistribution(typeDistribution)
                .departmentDistribution(departmentDistribution)
                .doctorDistribution(doctorDistribution)
                .dailyTrend(dailyTrend)
                .insights(insights)
                .build();
    }

    public PatientHistoryAnalysisResponse analyzePatientHistory(Long patientNo) {
        PatientEntity patient = patientService.getPatientByNo(patientNo);

        List<CheckInEntity> checkIns = checkInRepository.findByPatientEntity_PatientNo(patientNo);
        List<Long> checkInIds = checkIns.stream()
                .map(CheckInEntity::getCheckInId)
                .collect(Collectors.toList());

        List<TreatmentEntity> treatments = treatmentRepository.findByCheckInEntity_CheckInIdIn(checkInIds);

        if (treatments.isEmpty()) {
            return PatientHistoryAnalysisResponse.builder()
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
        }

        Map<String, Integer> typeDistribution = treatments.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTreatmentType() != null ? t.getTreatmentType().name() : "UNKNOWN",
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        Map<String, Integer> departmentDistribution = treatments.stream()
                .filter(t -> t.getTreatmentDept() != null)
                .collect(Collectors.groupingBy(
                        TreatmentEntity::getTreatmentDept,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        Map<String, Integer> doctorDistribution = treatments.stream()
                .filter(t -> t.getTreatmentDoc() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getTreatmentDoc().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        List<LocalDate> dates = treatments.stream()
                .map(t -> t.getTreatmentDate().toLocalDate())
                .sorted()
                .collect(Collectors.toList());

        PatientHistoryAnalysisResponse.RevisitPattern revisitPattern = analyzeRevisitPattern(dates);

        List<PatientHistoryAnalysisResponse.Insight> insights = generatePatientInsights(treatments, revisitPattern);

        return PatientHistoryAnalysisResponse.builder()
                .patientNo(patientNo)
                .patientName(patient.getPatientName())
                .totalTreatments(treatments.size())
                .firstVisit(dates.isEmpty() ? null : dates.get(0))
                .lastVisit(dates.isEmpty() ? null : dates.get(dates.size() - 1))
                .typeDistribution(typeDistribution)
                .departmentDistribution(departmentDistribution)
                .doctorDistribution(doctorDistribution)
                .revisitPattern(revisitPattern)
                .insights(insights)
                .build();
    }

    public AnomalyDetectionResponse detectAnomalies() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<TreatmentEntity> treatments = treatmentRepository.findByTreatmentDateBetween(startDateTime, endDateTime);

        List<AnomalyDetectionResponse.Anomaly> anomalies = new ArrayList<>();

        if (treatments.isEmpty()) {
            return AnomalyDetectionResponse.builder()
                    .anomalies(Collections.emptyList())
                    .totalDetected(0)
                    .build();
        }

        double avgPerDay = (double) treatments.size() / 7.0;

        Map<LocalDate, Long> dailyCount = treatments.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTreatmentDate().toLocalDate(),
                        Collectors.counting()
                ));

        dailyCount.forEach((date, count) -> {
            if (count > avgPerDay * 2) {
                anomalies.add(AnomalyDetectionResponse.Anomaly.builder()
                        .type("spike")
                        .date(date)
                        .count(count.intValue())
                        .message(String.format("%s: 평균(%.1f건) 대비 %d건으로 급증", 
                                date, avgPerDay, count))
                        .build());
            }
        });

        long emergencyCount = treatments.stream()
                .filter(t -> t.getTreatmentType() == TreatmentType.EMERGENCY)
                .count();

        double emergencyRatio = (double) emergencyCount / treatments.size();
        if (emergencyRatio > 0.5 && treatments.size() > 10) {
            anomalies.add(AnomalyDetectionResponse.Anomaly.builder()
                    .type("ratio")
                    .message(String.format("응급진료 비율이 %.1f%%로 비정상적으로 높습니다.", emergencyRatio * 100))
                    .build());
        }

        return AnomalyDetectionResponse.builder()
                .anomalies(anomalies)
                .totalDetected(anomalies.size())
                .build();
    }

    private List<TreatmentPatternAnalysisResponse.Insight> generateInsights(
            Map<String, Integer> typeDistribution,
            Map<String, Integer> departmentDistribution,
            Map<String, Integer> doctorDistribution,
            List<TreatmentEntity> treatments) {

        List<TreatmentPatternAnalysisResponse.Insight> insights = new ArrayList<>();

        if (!typeDistribution.isEmpty()) {
            String topType = typeDistribution.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");

            if (!topType.isEmpty()) {
                int count = typeDistribution.get(topType);
                double percentage = (double) count / treatments.size() * 100;
                insights.add(TreatmentPatternAnalysisResponse.Insight.builder()
                        .type("info")
                        .title("가장 많은 진료 유형")
                        .message(String.format("%s: %d건 (%.1f%%)", getTypeLabel(topType), count, percentage))
                        .build());
            }
        }

        if (!departmentDistribution.isEmpty()) {
            String topDept = departmentDistribution.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");

            if (!topDept.isEmpty()) {
                int count = departmentDistribution.get(topDept);
                insights.add(TreatmentPatternAnalysisResponse.Insight.builder()
                        .type("warning")
                        .title("가장 바쁜 진료과")
                        .message(String.format("%s: %d건 - 업무량 분산 검토 권장", topDept, count))
                        .build());
            }
        }

        if (!doctorDistribution.isEmpty()) {
            String topDoctor = doctorDistribution.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");

            if (!topDoctor.isEmpty()) {
                int count = doctorDistribution.get(topDoctor);
                double ratio = (double) count / treatments.size();
                if (ratio > 0.3) {
                    insights.add(TreatmentPatternAnalysisResponse.Insight.builder()
                            .type("warning")
                            .title("업무량 집중")
                            .message(String.format("%s 의사: %d건 - 업무 분산 권장", topDoctor, count))
                            .build());
                }
            }
        }

        return insights;
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
            long days = java.time.temporal.ChronoUnit.DAYS.between(dates.get(i - 1), dates.get(i));
            intervals.add((double) days);
        }

        double avgInterval = intervals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        String frequency = avgInterval < 30 ? "high" : avgInterval < 90 ? "medium" : "low";

        return PatientHistoryAnalysisResponse.RevisitPattern.builder()
                .frequency(frequency)
                .averageDays((int) Math.round(avgInterval))
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
                    .message("빈번한 재방문 패턴이 감지되었습니다. 정기적인 모니터링이 필요할 수 있습니다.")
                    .build());
        }

        long emergencyCount = treatments.stream()
                .filter(t -> t.getTreatmentType() == TreatmentType.EMERGENCY)
                .count();

        if (emergencyCount > 2) {
            insights.add(PatientHistoryAnalysisResponse.Insight.builder()
                    .type("warning")
                    .message("응급진료 이력이 많습니다. 지속적인 관리가 필요합니다.")
                    .build());
        }

        return insights;
    }

    private String getTypeLabel(String type) {
        try {
            TreatmentType treatmentType = TreatmentType.valueOf(type);
            return switch (treatmentType) {
                case IN -> "입원진료";
                case OUT -> "외래진료";
                case EMERGENCY -> "응급진료";
            };
        } catch (IllegalArgumentException e) {
            return type;
        }
    }
}

