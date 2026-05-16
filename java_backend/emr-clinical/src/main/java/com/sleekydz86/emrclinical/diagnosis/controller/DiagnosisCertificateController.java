package com.sleekydz86.emrclinical.diagnosis.controller;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.emrclinical.diagnosis.dto.DiagnosisCertificateCreateRequest;
import com.sleekydz86.emrclinical.diagnosis.dto.DiagnosisCertificateResponse;
import com.sleekydz86.emrclinical.diagnosis.entity.DiagnosisCertificateEntity;
import com.sleekydz86.emrclinical.diagnosis.service.DiagnosisCertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnosis/certificate")
@RequiredArgsConstructor
public class DiagnosisCertificateController {

    private final DiagnosisCertificateService certificateService;

    @PostMapping
    @AuthRole({"DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.CREATE)
    public ResponseEntity<DiagnosisCertificateResponse> issueCertificate(
            @Valid @RequestBody DiagnosisCertificateCreateRequest request) {
        DiagnosisCertificateEntity certificate = certificateService.issue(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DiagnosisCertificateResponse.from(certificate));
    }

    @GetMapping("/{certificateId}")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<DiagnosisCertificateResponse> getCertificate(@PathVariable Long certificateId) {
        DiagnosisCertificateEntity certificate = certificateService.findById(certificateId);
        return ResponseEntity.ok(DiagnosisCertificateResponse.from(certificate));
    }

    @GetMapping("/number/{certificateNumber}")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<DiagnosisCertificateResponse> getCertificateByNumber(
            @PathVariable String certificateNumber) {
        DiagnosisCertificateEntity certificate = certificateService.findByCertificateNumber(certificateNumber);
        return ResponseEntity.ok(DiagnosisCertificateResponse.from(certificate));
    }

    @GetMapping("/patient/{patientId}")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<Page<DiagnosisCertificateResponse>> getCertificatesByPatient(
            @PathVariable Long patientId,
            @PageableDefault(size = 20, sort = "issuedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DiagnosisCertificateEntity> certificates = certificateService.findByPatient(patientId, pageable);
        return ResponseEntity.ok(certificates.map(DiagnosisCertificateResponse::from));
    }

    @GetMapping("/doctor/{doctorId}")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<Page<DiagnosisCertificateResponse>> getCertificatesByDoctor(
            @PathVariable Long doctorId,
            @PageableDefault(size = 20, sort = "issuedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DiagnosisCertificateEntity> certificates = certificateService.findByDoctor(doctorId, pageable);
        return ResponseEntity.ok(certificates.map(DiagnosisCertificateResponse::from));
    }

    @PostMapping("/{certificateId}/revoke")
    @AuthRole({"DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.UPDATE)
    public ResponseEntity<Void> revokeCertificate(@PathVariable Long certificateId) {
        certificateService.revoke(certificateId);
        return ResponseEntity.ok().build();
    }
}
