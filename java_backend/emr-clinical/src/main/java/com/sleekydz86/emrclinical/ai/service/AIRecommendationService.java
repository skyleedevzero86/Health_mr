package com.sleekydz86.emrclinical.ai.service;

import com.sleekydz86.core.audit.service.AuditService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.service.UserService;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.emrclinical.ai.dto.DoctorRecommendationRequest;
import com.sleekydz86.emrclinical.ai.dto.DoctorRecommendationResponse;
import com.sleekydz86.emrclinical.ai.dto.PatientHistoryAnalysisResponse;
import com.sleekydz86.emrclinical.ai.dto.TreatmentRecommendationRequest;
import com.sleekydz86.emrclinical.ai.dto.TreatmentRecommendationResponse;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.emrclinical.types.TreatmentType;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AIRecommendationService {

    private final TreatmentRepository treatmentRepository;
    private final UserService userService;
    private final AIAnalysisService aiAnalysisService;
    private final AuditService auditService;

    public TreatmentRecommendationResponse recommendTreatmentType(Long userId, TreatmentRecommendationRequest request) {
        String symptoms = request.getSymptoms().toLowerCase();
        List<TreatmentRecommendationResponse.Alternative> alternatives = new ArrayList<>();
        TreatmentType recommendedType;
        double confidence;
        String reason;

        if (containsAny(symptoms, "응급", "긴급", "심한", "급성", "출혈", "의식불명")) {
            recommendedType = TreatmentType.EMERGENCY;
            confidence = 0.9;
            reason = "응급 징후가 보여 응급 진료를 우선 권장합니다.";
            alternatives.add(TreatmentRecommendationResponse.Alternative.builder()
                    .type(TreatmentType.IN)
                    .confidence(0.7)
                    .reason("상태에 따라 입원 진료가 이어질 수 있습니다.")
                    .build());
        } else if (containsAny(symptoms, "입원", "수술", "중증", "입원 필요")) {
            recommendedType = TreatmentType.IN;
            confidence = 0.8;
            reason = "입원 치료 가능성이 높은 증상으로 판단했습니다.";
            alternatives.add(TreatmentRecommendationResponse.Alternative.builder()
                    .type(TreatmentType.OUT)
                    .confidence(0.6)
                    .reason("외래 진료 후 입원 여부를 다시 판단할 수 있습니다.")
                    .build());
        } else {
            recommendedType = TreatmentType.OUT;
            confidence = 0.7;
            reason = "현재 정보 기준으로는 외래 진료가 적절합니다.";
            alternatives.add(TreatmentRecommendationResponse.Alternative.builder()
                    .type(TreatmentType.IN)
                    .confidence(0.5)
                    .reason("증상 악화 시 입원 전환이 필요할 수 있습니다.")
                    .build());
        }

        Map<String, Object> sourceData = new LinkedHashMap<>();
        sourceData.put("sourceDatabase", "MYSQL");
        sourceData.put("patientNo", request.getPatientNo());
        sourceData.put("historyLookupUsed", false);

        if (request.getPatientNo() != null && request.getPatientHistory() != null) {
            try {
                PatientHistoryAnalysisResponse history = aiAnalysisService.analyzePatientHistoryInternal(request.getPatientNo());
                sourceData.put("historyLookupUsed", true);
                sourceData.put("historyTreatmentCount", history.getTotalTreatments());
                sourceData.put("historyRevisitFrequency",
                        history.getRevisitPattern() != null ? history.getRevisitPattern().getFrequency() : null);

                if (history.getRevisitPattern() != null && "high".equals(history.getRevisitPattern().getFrequency())) {
                    confidence = Math.min(confidence + 0.1, 1.0);
                    reason += " 기존 내원 이력을 함께 고려했습니다.";
                }
            } catch (Exception exception) {
                log.debug("환자 이력 보조 조회에 실패했습니다. 기본 추천으로 진행합니다. {}", exception.getMessage());
            }
        }

        TreatmentRecommendationResponse response = TreatmentRecommendationResponse.builder()
                .recommendedType(recommendedType)
                .confidence(Math.min(confidence, 1.0))
                .reason(reason)
                .alternatives(alternatives)
                .build();

        Map<String, Object> responseSummary = new LinkedHashMap<>();
        responseSummary.put("recommendedType", response.getRecommendedType());
        responseSummary.put("confidence", response.getConfidence());
        responseSummary.put("alternativeCount", alternatives.size());

        auditUsage(userId, "TREATMENT_RECOMMENDATION", request, sourceData, responseSummary);
        return response;
    }

    public DoctorRecommendationResponse recommendDoctor(Long userId, DoctorRecommendationRequest request) {
        List<UserEntity> doctors = userService.getUsersByRole(RoleType.DOCTOR);
        List<UserEntity> departmentDoctors = doctors.stream()
                .filter(doctor -> doctor.getDepartment() != null)
                .filter(doctor -> request.getDepartment().equals(doctor.getDepartment().getName()))
                .toList();

        Map<String, Object> sourceData = new LinkedHashMap<>();
        sourceData.put("sourceDatabase", "MYSQL");
        sourceData.put("doctorCandidateCount", doctors.size());
        sourceData.put("departmentDoctorCount", departmentDoctors.size());
        sourceData.put("department", request.getDepartment());
        sourceData.put("treatmentType", request.getTreatmentType());
        sourceData.put("urgency", request.getUrgency());

        DoctorRecommendationResponse response;
        if (departmentDoctors.isEmpty()) {
            response = DoctorRecommendationResponse.builder()
                    .message("해당 진료과에서 추천 가능한 의사를 찾지 못했습니다.")
                    .build();
        } else {
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            LocalDateTime now = LocalDateTime.now();
            Map<Long, DoctorWorkload> workloads = new HashMap<>();

            for (UserEntity doctor : departmentDoctors) {
                List<TreatmentEntity> recentTreatments = treatmentRepository.findByConditions(
                        null,
                        doctor.getId(),
                        null,
                        null,
                        null,
                        weekAgo,
                        now
                );
                int experience = treatmentRepository.findByConditions(
                        null,
                        doctor.getId(),
                        null,
                        request.getTreatmentType(),
                        null,
                        null,
                        null
                ).size();

                workloads.put(doctor.getId(), DoctorWorkload.builder()
                        .doctor(doctor)
                        .workload(recentTreatments.size())
                        .experience(experience)
                        .build());
            }

            List<DoctorWorkload> sorted = workloads.values().stream()
                    .sorted((left, right) -> {
                        if ("high".equalsIgnoreCase(request.getUrgency())) {
                            return Integer.compare(right.getExperience(), left.getExperience());
                        }
                        return Integer.compare(left.getWorkload(), right.getWorkload());
                    })
                    .toList();

            DoctorWorkload recommended = sorted.get(0);
            List<DoctorRecommendationResponse.RecommendedDoctor> alternatives = sorted.stream()
                    .skip(1)
                    .limit(2)
                    .map(this::toDoctorResponse)
                    .toList();

            double confidence = recommended.getWorkload() < 10 ? 0.9 : 0.7;
            String reason = "high".equalsIgnoreCase(request.getUrgency())
                    ? "긴급 요청으로 경험도가 높은 의사를 우선 추천했습니다."
                    : "현재 업무량이 상대적으로 낮은 의사를 우선 추천했습니다.";

            response = DoctorRecommendationResponse.builder()
                    .recommended(toDoctorResponse(recommended))
                    .confidence(confidence)
                    .reason(reason)
                    .alternatives(alternatives)
                    .build();

            sourceData.put("evaluatedDoctorCount", sorted.size());
            sourceData.put("selectedDoctorId", recommended.getDoctor().getId());
        }

        Map<String, Object> responseSummary = new LinkedHashMap<>();
        responseSummary.put("hasRecommendation", response.getRecommended() != null);
        responseSummary.put("alternativeCount", response.getAlternatives() != null ? response.getAlternatives().size() : 0);
        responseSummary.put("message", response.getMessage());

        auditUsage(userId, "DOCTOR_RECOMMENDATION", request, sourceData, responseSummary);
        return response;
    }

    private boolean containsAny(String target, String... keywords) {
        for (String keyword : keywords) {
            if (target.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private DoctorRecommendationResponse.RecommendedDoctor toDoctorResponse(DoctorWorkload workload) {
        String departmentName = workload.getDoctor().getDepartment() != null
                ? workload.getDoctor().getDepartment().getName()
                : "";

        return DoctorRecommendationResponse.RecommendedDoctor.builder()
                .userId(workload.getDoctor().getId())
                .name(workload.getDoctor().getName())
                .department(departmentName)
                .workload(workload.getWorkload())
                .experience(workload.getExperience())
                .build();
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

    @Value
    @Builder
    private static class DoctorWorkload {
        UserEntity doctor;
        int workload;
        int experience;
    }
}
