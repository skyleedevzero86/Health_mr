package com.sleekydz86.emrclinical.ai.service;

import com.sleekydz86.core.audit.service.AuditService;
import com.sleekydz86.emrclinical.ai.dto.ScheduleOptimizationRequest;
import com.sleekydz86.emrclinical.ai.dto.ScheduleOptimizationResponse;
import com.sleekydz86.emrclinical.ai.dto.TreatmentReportRequest;
import com.sleekydz86.emrclinical.ai.dto.TreatmentReportResponse;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AIReportService {

    private final TreatmentRepository treatmentRepository;
    private final AuditService auditService;

    public TreatmentReportResponse generateTreatmentReport(Long userId, TreatmentReportRequest request) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        String title;

        switch (request.getReportType()) {
            case "daily" -> {
                startDate = now;
                endDate = now;
                title = "일일 진료 보고서";
            }
            case "weekly" -> {
                int dayOfWeek = now.getDayOfWeek().getValue() - 1;
                startDate = now.minusDays(dayOfWeek);
                endDate = startDate.plusDays(6);
                title = "주간 진료 보고서";
            }
            case "monthly" -> {
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
                title = "월간 진료 보고서";
            }
            default -> {
                startDate = request.getStartDate() != null ? request.getStartDate() : now;
                endDate = request.getEndDate() != null ? request.getEndDate() : now;
                title = "맞춤 진료 보고서";
            }
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<TreatmentEntity> treatments = treatmentRepository.findByTreatmentDateBetween(startDateTime, endDateTime);

        Map<String, Integer> typeStats = countBy(
                treatments.stream()
                        .map(treatment -> treatment.getTreatmentType() != null
                                ? treatment.getTreatmentType().name()
                                : "UNKNOWN")
                        .toList());
        Map<String, Integer> departmentStats = countBy(
                treatments.stream()
                        .filter(treatment -> treatment.getTreatmentDept() != null)
                        .map(TreatmentEntity::getTreatmentDept)
                        .toList());
        Map<String, Integer> statusStats = countBy(
                treatments.stream()
                        .map(treatment -> treatment.getTreatmentStatus() != null
                                ? treatment.getTreatmentStatus().name()
                                : "UNKNOWN")
                        .toList());

        Set<Long> patientNos = treatments.stream()
                .filter(treatment -> treatment.getCheckInEntity() != null)
                .map(treatment -> treatment.getCheckInEntity().getPatientEntity().getPatientNoValue())
                .collect(java.util.stream.Collectors.toSet());
        Set<String> doctorNames = treatments.stream()
                .filter(treatment -> treatment.getTreatmentDoc() != null)
                .map(treatment -> treatment.getTreatmentDoc().getName())
                .collect(java.util.stream.Collectors.toSet());

        Map<String, Integer> doctorCounts = countBy(
                treatments.stream()
                        .filter(treatment -> treatment.getTreatmentDoc() != null)
                        .map(treatment -> treatment.getTreatmentDoc().getName())
                        .toList());

        List<TreatmentReportResponse.DoctorStat> topDoctors = doctorCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> TreatmentReportResponse.DoctorStat.builder()
                        .doctorName(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();

        TreatmentReportResponse response = TreatmentReportResponse.builder()
                .title(title)
                .period(startDate + " ~ " + endDate)
                .generatedAt(LocalDateTime.now())
                .summary(TreatmentReportResponse.Summary.builder()
                        .totalTreatments(treatments.size())
                        .totalPatients(patientNos.size())
                        .totalDoctors(doctorNames.size())
                        .build())
                .statistics(TreatmentReportResponse.Statistics.builder()
                        .byType(typeStats)
                        .byDepartment(departmentStats)
                        .byStatus(statusStats)
                        .build())
                .topDoctors(topDoctors)
                .content(buildReportContent(title, startDate, endDate, treatments.size(), patientNos.size(), doctorNames.size(), typeStats, departmentStats, topDoctors))
                .format(request.getFormat() != null ? request.getFormat() : "html")
                .build();

        Map<String, Object> sourceData = new LinkedHashMap<>();
        sourceData.put("sourceDatabase", "MYSQL");
        sourceData.put("startDate", startDate);
        sourceData.put("endDate", endDate);
        sourceData.put("treatmentCount", treatments.size());
        sourceData.put("patientCount", patientNos.size());
        sourceData.put("doctorCount", doctorNames.size());

        Map<String, Object> responseSummary = new LinkedHashMap<>();
        responseSummary.put("title", response.getTitle());
        responseSummary.put("format", response.getFormat());
        responseSummary.put("topDoctorCount", topDoctors.size());

        auditUsage(userId, "TREATMENT_REPORT", request, sourceData, responseSummary);
        return response;
    }

    public ScheduleOptimizationResponse optimizeSchedule(Long userId, ScheduleOptimizationRequest request) {
        LocalDate targetDate = request.getDate() != null ? request.getDate() : LocalDate.now();
        LocalDateTime startDateTime = targetDate.atStartOfDay();
        LocalDateTime endDateTime = targetDate.atTime(23, 59, 59);
        List<TreatmentEntity> treatments = treatmentRepository.findByTreatmentDateBetween(startDateTime, endDateTime);

        if (request.getDoctorId() != null) {
            treatments = treatments.stream()
                    .filter(treatment -> treatment.getTreatmentDoc() != null)
                    .filter(treatment -> request.getDoctorId().equals(treatment.getTreatmentDoc().getId()))
                    .toList();
        }

        Map<String, List<Long>> doctorSchedule = new LinkedHashMap<>();
        for (TreatmentEntity treatment : treatments) {
            if (treatment.getTreatmentDoc() == null) {
                continue;
            }
            doctorSchedule.computeIfAbsent(treatment.getTreatmentDoc().getName(), key -> new ArrayList<>())
                    .add(treatment.getTreatmentId());
        }

        List<ScheduleOptimizationResponse.Suggestion> suggestions = new ArrayList<>();
        doctorSchedule.forEach((doctorName, treatmentIds) -> {
            if (treatmentIds.size() > 8) {
                suggestions.add(ScheduleOptimizationResponse.Suggestion.builder()
                        .type("warning")
                        .doctor(doctorName)
                        .message(String.format("%s 의사의 당일 진료 예약이 %d건으로 많습니다.", doctorName, treatmentIds.size()))
                        .recommendation("일부 예약을 다른 의사에게 분산하는 것을 권장합니다.")
                        .build());
            }
        });

        ScheduleOptimizationResponse response = ScheduleOptimizationResponse.builder()
                .date(targetDate)
                .totalTreatments(treatments.size())
                .doctorSchedule(doctorSchedule)
                .suggestions(suggestions)
                .optimization(suggestions.isEmpty()
                        ? "현재 스케줄은 비교적 안정적인 상태입니다."
                        : "업무량 분산이 필요한 스케줄이 감지되었습니다.")
                .build();

        Map<String, Object> sourceData = new LinkedHashMap<>();
        sourceData.put("sourceDatabase", "MYSQL");
        sourceData.put("targetDate", targetDate);
        sourceData.put("doctorId", request.getDoctorId());
        sourceData.put("treatmentCount", treatments.size());
        sourceData.put("doctorCount", doctorSchedule.size());

        Map<String, Object> responseSummary = new LinkedHashMap<>();
        responseSummary.put("suggestionCount", suggestions.size());
        responseSummary.put("optimization", response.getOptimization());

        auditUsage(userId, "SCHEDULE_OPTIMIZATION", request, sourceData, responseSummary);
        return response;
    }

    private Map<String, Integer> countBy(List<String> values) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String value : values) {
            counts.merge(value, 1, Integer::sum);
        }
        return counts;
    }

    private String buildReportContent(
            String title,
            LocalDate startDate,
            LocalDate endDate,
            int totalTreatments,
            int totalPatients,
            int totalDoctors,
            Map<String, Integer> typeStats,
            Map<String, Integer> departmentStats,
            List<TreatmentReportResponse.DoctorStat> topDoctors) {
        StringBuilder builder = new StringBuilder();
        builder.append("<h2>").append(title).append("</h2>\n");
        builder.append("<p><strong>기간:</strong> ").append(startDate).append(" ~ ").append(endDate).append("</p>\n");
        builder.append("<ul>\n");
        builder.append("<li>총 진료 건수: ").append(totalTreatments).append("건</li>\n");
        builder.append("<li>총 환자 수: ").append(totalPatients).append("명</li>\n");
        builder.append("<li>참여 의사 수: ").append(totalDoctors).append("명</li>\n");
        builder.append("</ul>\n");

        builder.append("<h3>진료 유형 통계</h3>\n<ul>\n");
        typeStats.forEach((type, count) -> builder.append("<li>").append(type).append(": ").append(count).append("건</li>\n"));
        builder.append("</ul>\n");

        builder.append("<h3>진료과 통계</h3>\n<ul>\n");
        departmentStats.forEach((department, count) -> builder.append("<li>").append(department).append(": ").append(count).append("건</li>\n"));
        builder.append("</ul>\n");

        builder.append("<h3>상위 의사</h3>\n<ol>\n");
        topDoctors.forEach(doctor -> builder.append("<li>")
                .append(doctor.getDoctorName())
                .append(": ")
                .append(doctor.getCount())
                .append("건</li>\n"));
        builder.append("</ol>\n");
        return builder.toString();
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
