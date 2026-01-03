package com.sleekydz86.finance.qualification.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.finance.qualification.service.QualificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/api/qualification")
@RequiredArgsConstructor
public class QualificationController {

    private final QualificationService qualificationService;

    @GetMapping("/patient/{patientNo}")
    @AuthRole({RoleType.STAFF, RoleType.ADMIN, RoleType.DOCTOR})
    public Mono<ResponseEntity<Map<String, Object>>> getAllQualifications(@PathVariable Long patientNo) {
        return qualificationService.getAllQualifications(patientNo)
                .map(response -> ResponseEntity.ok(Map.of(
                        "message", "자격 정보 조회 성공",
                        "patientNo", patientNo,
                        "data", response
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "자격 정보 조회 실패"
                )));
    }

    @GetMapping("/patient/{patientNo}/health-insurance")
    @AuthRole({RoleType.STAFF, RoleType.ADMIN, RoleType.DOCTOR})
    public Mono<ResponseEntity<Map<String, Object>>> getHealthInsurance(@PathVariable Long patientNo) {
        return qualificationService.getHealthInsuranceInfo(patientNo)
                .map(response -> ResponseEntity.ok(Map.of(
                        "message", "건강보험 자격 조회 성공",
                        "patientNo", patientNo,
                        "data", response
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "건강보험 자격 조회 실패"
                )));
    }

    @GetMapping("/patient/{patientNo}/medical-assistance")
    @AuthRole({RoleType.STAFF, RoleType.ADMIN, RoleType.DOCTOR})
    public Mono<ResponseEntity<Map<String, Object>>> getMedicalAssistance(@PathVariable Long patientNo) {
        return qualificationService.getMedicalAssistanceInfo(patientNo)
                .map(response -> ResponseEntity.ok(Map.of(
                        "message", "의료급여 자격 조회 성공",
                        "patientNo", patientNo,
                        "data", response
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "의료급여 자격 조회 실패"
                )));
    }

    @GetMapping("/patient/{patientNo}/basic-livelihood")
    @AuthRole({RoleType.STAFF, RoleType.ADMIN, RoleType.DOCTOR})
    public Mono<ResponseEntity<Map<String, Object>>> getBasicLivelihood(@PathVariable Long patientNo) {
        return qualificationService.getBasicLivelihoodInfo(patientNo)
                .map(response -> ResponseEntity.ok(Map.of(
                        "message", "기초생활수급자 자격 조회 성공",
                        "patientNo", patientNo,
                        "data", response
                )))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "기초생활수급자 자격 조회 실패"
                )));
    }
}

