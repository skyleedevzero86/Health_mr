package com.sleekydz86.emrclinical.ai.service;

import com.sleekydz86.emrclinical.ai.dto.ScheduleOptimizationRequest;
import com.sleekydz86.emrclinical.ai.dto.ScheduleOptimizationResponse;
import com.sleekydz86.emrclinical.ai.dto.TreatmentReportRequest;
import com.sleekydz86.emrclinical.ai.dto.TreatmentReportResponse;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.checkin.repository.CheckInRepository;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
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
public class AIReportService {

    private final TreatmentRepository treatmentRepository;
    private final CheckInRepository checkInRepository;

    public TreatmentReportResponse generateTreatmentReport(TreatmentReportRequest request) {
        LocalDate startDate;
        LocalDate endDate;
        String title;

        LocalDate now = LocalDate.now();

        if ("daily".equals(request.getReportType())) {
            startDate = now;
            endDate = now;
            title = "일일 진료 보고서";
        } else if ("weekly".equals(request.getReportType())) {
            int dayOfWeek = now.getDayOfWeek().getValue() - 1;
            startDate = now.minusDays(dayOfWeek);
            endDate = startDate.plusDays(6);
            title = "주간 진료 보고서";
        } else if ("monthly".equals(request.getReportType())) {
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
            title = "월간 진료 보고서";
        } else {
            startDate = request.getStartDate() != null ? request.getStartDate() : now;
            endDate = request.getEndDate() != null ? request.getEndDate() : now;
            title = "진료 보고서";
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<TreatmentEntity> treatments = treatmentRepository.findByTreatmentDateBetween(startDateTime, endDateTime);

        Map<String, Integer> typeStats = treatments.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTreatmentType() != null ? t.getTreatmentType().name() : "UNKNOWN",
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        Map<String, Integer> deptStats = treatments.stream()
                .filter(t -> t.getTreatmentDept() != null)
                .collect(Collectors.groupingBy(
                        TreatmentEntity::getTreatmentDept,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        Map<String, Integer> statusStats = treatments.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTreatmentStatus() != null ? t.getTreatmentStatus().name() : "UNKNOWN",
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        Set<Long> patientNos = treatments.stream()
                .filter(t -> t.getCheckInEntity() != null)
                .map(t -> t.getCheckInEntity().getPatientEntity().getPatientNoValue())
                .collect(Collectors.toSet());

        Set<String> doctorNames = treatments.stream()
                .filter(t -> t.getTreatmentDoc() != null)
                .map(t -> t.getTreatmentDoc().getName())
                .collect(Collectors.toSet());

        Map<String, Integer> doctorCounts = treatments.stream()
                .filter(t -> t.getTreatmentDoc() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getTreatmentDoc().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        List<TreatmentReportResponse.DoctorStat> topDoctors = doctorCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> TreatmentReportResponse.DoctorStat.builder()
                        .doctorName(e.getKey())
                        .count(e.getValue())
                        .build())
                .collect(Collectors.toList());

        String content = generateReportContent(title, startDate, endDate, treatments.size(), 
                patientNos.size(), doctorNames.size(), typeStats, deptStats, statusStats, topDoctors);

        return TreatmentReportResponse.builder()
                .title(title)
                .period(String.format("%s ~ %s", startDate, endDate))
                .generatedAt(LocalDateTime.now())
                .summary(TreatmentReportResponse.Summary.builder()
                        .totalTreatments(treatments.size())
                        .totalPatients(patientNos.size())
                        .totalDoctors(doctorNames.size())
                        .build())
                .statistics(TreatmentReportResponse.Statistics.builder()
                        .byType(typeStats)
                        .byDepartment(deptStats)
                        .byStatus(statusStats)
                        .build())
                .topDoctors(topDoctors)
                .content(content)
                .format(request.getFormat() != null ? request.getFormat() : "html")
                .build();
    }

    public ScheduleOptimizationResponse optimizeSchedule(ScheduleOptimizationRequest request) {
        LocalDate targetDate = request.getDate() != null ? request.getDate() : LocalDate.now();
        LocalDateTime startDateTime = targetDate.atStartOfDay();
        LocalDateTime endDateTime = targetDate.atTime(23, 59, 59);

        List<TreatmentEntity> treatments = treatmentRepository.findByTreatmentDateBetween(startDateTime, endDateTime);

        if (request.getDoctorId() != null) {
            treatments = treatments.stream()
                    .filter(t -> t.getTreatmentDoc() != null && 
                            t.getTreatmentDoc().getId().equals(request.getDoctorId()))
                    .collect(Collectors.toList());
        }

        Map<String, List<Long>> doctorSchedule = treatments.stream()
                .filter(t -> t.getTreatmentDoc() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getTreatmentDoc().getName(),
                        Collectors.mapping(TreatmentEntity::getTreatmentId, Collectors.toList())
                ));

        List<ScheduleOptimizationResponse.Suggestion> suggestions = new ArrayList<>();

        doctorSchedule.forEach((doctor, treatmentIds) -> {
            if (treatmentIds.size() > 8) {
                suggestions.add(ScheduleOptimizationResponse.Suggestion.builder()
                        .type("warning")
                        .doctor(doctor)
                        .message(String.format("%s 의사: %d건 예약 - 업무량 과다", doctor, treatmentIds.size()))
                        .recommendation("일부 진료를 다른 의사에게 재배정 권장")
                        .build());
            }
        });

        String optimization = suggestions.isEmpty() 
                ? "일정이 최적화되어 있습니다." 
                : "일정 재배정이 필요합니다.";

        return ScheduleOptimizationResponse.builder()
                .date(targetDate)
                .totalTreatments(treatments.size())
                .doctorSchedule(doctorSchedule)
                .suggestions(suggestions)
                .optimization(optimization)
                .build();
    }

    private String generateReportContent(String title, LocalDate startDate, LocalDate endDate,
                                        int totalTreatments, int totalPatients, int totalDoctors,
                                        Map<String, Integer> typeStats, Map<String, Integer> deptStats,
                                        Map<String, Integer> statusStats, 
                                        List<TreatmentReportResponse.DoctorStat> topDoctors) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>").append(title).append("</h2>\n");
        sb.append("<p><strong>보고 기간:</strong> ").append(startDate).append(" ~ ").append(endDate).append("</p>\n");
        sb.append("<h3>요약</h3>\n");
        sb.append("<ul>\n");
        sb.append("<li>총 진료 건수: ").append(totalTreatments).append("건</li>\n");
        sb.append("<li>총 환자 수: ").append(totalPatients).append("명</li>\n");
        sb.append("<li>참여 의사 수: ").append(totalDoctors).append("명</li>\n");
        sb.append("</ul>\n");
        
        sb.append("<h3>진료 유형별 통계</h3>\n<ul>\n");
        typeStats.forEach((type, count) -> 
                sb.append("<li>").append(type).append(": ").append(count).append("건</li>\n"));
        sb.append("</ul>\n");
        
        sb.append("<h3>진료과별 통계</h3>\n<ul>\n");
        deptStats.forEach((dept, count) -> 
                sb.append("<li>").append(dept).append(": ").append(count).append("건</li>\n"));
        sb.append("</ul>\n");
        
        sb.append("<h3>상위 의사 (진료 건수 기준)</h3>\n<ol>\n");
        topDoctors.forEach(doctor -> 
                sb.append("<li>").append(doctor.getDoctorName()).append(": ").append(doctor.getCount()).append("건</li>\n"));
        sb.append("</ol>\n");
        
        return sb.toString();
    }
}

