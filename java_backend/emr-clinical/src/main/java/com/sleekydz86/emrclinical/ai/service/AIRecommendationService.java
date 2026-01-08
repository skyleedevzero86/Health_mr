package com.sleekydz86.emrclinical.ai.service;

import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.service.UserService;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.emrclinical.ai.dto.DoctorRecommendationRequest;
import com.sleekydz86.emrclinical.ai.dto.DoctorRecommendationResponse;
import com.sleekydz86.emrclinical.ai.dto.TreatmentRecommendationRequest;
import com.sleekydz86.emrclinical.ai.dto.TreatmentRecommendationResponse;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.emrclinical.types.TreatmentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AIRecommendationService {

    private final TreatmentRepository treatmentRepository;
    private final UserService userService;
    private final AIAnalysisService aiAnalysisService;

    public TreatmentRecommendationResponse recommendTreatmentType(TreatmentRecommendationRequest request) {
        String symptoms = request.getSymptoms().toLowerCase();
        List<TreatmentRecommendationResponse.Alternative> alternatives = new ArrayList<>();
        TreatmentType recommendedType;
        double confidence;
        String reason;

        if (symptoms.contains("응급") || symptoms.contains("긴급") || 
            symptoms.contains("심한") || symptoms.contains("급성") ||
            symptoms.contains("출혈") || symptoms.contains("의식불명")) {
            recommendedType = TreatmentType.EMERGENCY;
            confidence = 0.9;
            reason = "응급 증상이 감지되어 응급진료를 권장합니다.";
            
            alternatives.add(TreatmentRecommendationResponse.Alternative.builder()
                    .type(TreatmentType.IN)
                    .confidence(0.7)
                    .reason("입원이 필요할 수 있습니다.")
                    .build());
        } else if (symptoms.contains("입원") || symptoms.contains("수술") ||
                   symptoms.contains("중증") || symptoms.contains("입원 필요")) {
            recommendedType = TreatmentType.IN;
            confidence = 0.8;
            reason = "입원이 필요한 증상으로 보입니다.";
            
            alternatives.add(TreatmentRecommendationResponse.Alternative.builder()
                    .type(TreatmentType.OUT)
                    .confidence(0.6)
                    .reason("외래진료로 시작 후 필요시 입원 고려")
                    .build());
        } else {
            recommendedType = TreatmentType.OUT;
            confidence = 0.7;
            reason = "외래진료로 충분할 것으로 판단됩니다.";
            
            alternatives.add(TreatmentRecommendationResponse.Alternative.builder()
                    .type(TreatmentType.IN)
                    .confidence(0.5)
                    .reason("증상 악화 시 입원 고려")
                    .build());
        }

        if (request.getPatientNo() != null && request.getPatientHistory() != null) {
            try {
                var history = aiAnalysisService.analyzePatientHistory(request.getPatientNo());
                if (history.getRevisitPattern() != null && 
                    "high".equals(history.getRevisitPattern().getFrequency())) {
                    confidence += 0.1;
                    reason += " (과거 재방문 이력 고려)";
                }
            } catch (Exception e) {
                log.debug("환자 이력 분석 실패, 기본 추천 사용: {}", e.getMessage());
            }
        }

        confidence = Math.min(confidence, 1.0);

        return TreatmentRecommendationResponse.builder()
                .recommendedType(recommendedType)
                .confidence(confidence)
                .reason(reason)
                .alternatives(alternatives)
                .build();
    }

    public DoctorRecommendationResponse recommendDoctor(DoctorRecommendationRequest request) {
        List<UserEntity> doctors = userService.getUsersByRole(RoleType.DOCTOR);
        
        List<UserEntity> deptDoctors = doctors.stream()
                .filter(d -> {
                    if (d.getDepartment() == null) {
                        return false;
                    }
                    String deptName = d.getDepartment().getName();
                    return deptName != null && deptName.equals(request.getDepartment());
                })
                .collect(Collectors.toList());

        if (deptDoctors.isEmpty()) {
            return DoctorRecommendationResponse.builder()
                    .message("해당 진료과의 의사를 찾을 수 없습니다.")
                    .build();
        }

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime now = LocalDateTime.now();

        Map<Long, DoctorWorkload> doctorWorkloads = new HashMap<>();
        
        for (UserEntity doctor : deptDoctors) {
            List<TreatmentEntity> recentTreatments = treatmentRepository.findByTreatmentDoc_Id(doctor.getId())
                    .stream()
                    .filter(t -> t.getTreatmentDate().isAfter(weekAgo) && 
                                t.getTreatmentDate().isBefore(now))
                    .collect(Collectors.toList());

            long experience = treatmentRepository.findByTreatmentDoc_Id(doctor.getId())
                    .stream()
                    .filter(t -> t.getTreatmentType() == request.getTreatmentType())
                    .count();

            doctorWorkloads.put(doctor.getId(), DoctorWorkload.builder()
                    .doctor(doctor)
                    .workload(recentTreatments.size())
                    .experience((int) experience)
                    .build());
        }

        List<DoctorWorkload> sorted = doctorWorkloads.values().stream()
                .sorted((a, b) -> {
                    if ("high".equals(request.getUrgency())) {
                        return Integer.compare(b.experience, a.experience);
                    } else {
                        return Integer.compare(a.workload, b.workload);
                    }
                })
                .collect(Collectors.toList());

        if (sorted.isEmpty()) {
            return DoctorRecommendationResponse.builder()
                    .message("추천할 의사를 찾을 수 없습니다.")
                    .build();
        }

        DoctorWorkload recommended = sorted.get(0);
        double confidence = recommended.workload < 10 ? 0.9 : 0.7;
        String reason = "high".equals(request.getUrgency())
                ? "해당 진료 유형에 경험이 많은 의사입니다."
                : "현재 업무량이 적어 여유가 있는 의사입니다.";

        List<DoctorRecommendationResponse.RecommendedDoctor> alternatives = sorted.stream()
                .skip(1)
                .limit(2)
                .map(w -> {
                    String deptName = "";
                    if (w.doctor.getDepartment() != null && w.doctor.getDepartment().getName() != null) {
                        deptName = w.doctor.getDepartment().getName();
                    }
                    return DoctorRecommendationResponse.RecommendedDoctor.builder()
                            .userId(w.doctor.getId())
                            .name(w.doctor.getName())
                            .department(deptName)
                            .workload(w.workload)
                            .experience(w.experience)
                            .build();
                })
                .collect(Collectors.toList());

        String deptName = "";
        if (recommended.doctor.getDepartment() != null && 
            recommended.doctor.getDepartment().getName() != null) {
            deptName = recommended.doctor.getDepartment().getName();
        }
        
        return DoctorRecommendationResponse.builder()
                .recommended(DoctorRecommendationResponse.RecommendedDoctor.builder()
                        .userId(recommended.doctor.getId())
                        .name(recommended.doctor.getName())
                        .department(deptName)
                        .workload(recommended.workload)
                        .experience(recommended.experience)
                        .build())
                .confidence(confidence)
                .reason(reason)
                .alternatives(alternatives)
                .build();
    }

    @lombok.Value
    @lombok.Builder
    private static class DoctorWorkload {
        UserEntity doctor;
        int workload;
        int experience;
    }
}

