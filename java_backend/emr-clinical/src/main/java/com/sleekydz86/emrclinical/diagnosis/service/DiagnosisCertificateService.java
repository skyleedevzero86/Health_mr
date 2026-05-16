package com.sleekydz86.emrclinical.diagnosis.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.emrclinical.diagnosis.dto.DiagnosisCertificateCreateRequest;
import com.sleekydz86.emrclinical.diagnosis.entity.DiagnosisCertificateEntity;
import com.sleekydz86.emrclinical.diagnosis.entity.KcdCodeEntity;
import com.sleekydz86.emrclinical.diagnosis.repository.DiagnosisCertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosisCertificateService {

    private final DiagnosisCertificateRepository certificateRepository;
    private final KcdCodeService kcdCodeService;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    @Transactional
    public DiagnosisCertificateEntity issue(DiagnosisCertificateCreateRequest request) {
        PatientEntity patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new NotFoundException("환자를 찾을 수 없습니다: " + request.getPatientId()));

        UserEntity doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new NotFoundException("의사를 찾을 수 없습니다: " + request.getDoctorId()));

        KcdCodeEntity kcdCode = kcdCodeService.findByCode(request.getKcdCode());

        DiagnosisCertificateEntity certificate = DiagnosisCertificateEntity.builder()
                .certificateNumber(generateCertificateNumber())
                .patient(patient)
                .doctor(doctor)
                .kcdCode(kcdCode)
                .diagnosisName(request.getDiagnosisName())
                .diagnosisDate(request.getDiagnosisDate())
                .clinicalFindings(request.getClinicalFindings())
                .purpose(request.getPurpose())
                .issuedAt(LocalDateTime.now())
                .build();

        return certificateRepository.save(certificate);
    }

    public DiagnosisCertificateEntity findById(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("진단서를 찾을 수 없습니다: " + id));
    }

    public DiagnosisCertificateEntity findByCertificateNumber(String number) {
        return certificateRepository.findByCertificateNumber(number)
                .orElseThrow(() -> new NotFoundException("진단서를 찾을 수 없습니다: " + number));
    }

    public Page<DiagnosisCertificateEntity> findByPatient(Long patientId, Pageable pageable) {
        return certificateRepository.findByPatient_Id(patientId, pageable);
    }

    public Page<DiagnosisCertificateEntity> findByDoctor(Long doctorId, Pageable pageable) {
        return certificateRepository.findByDoctor_Id(doctorId, pageable);
    }

    @Transactional
    public void revoke(Long certificateId) {
        DiagnosisCertificateEntity certificate = findById(certificateId);
        certificate.revoke();
    }

    private String generateCertificateNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = SEQUENCE.getAndIncrement();
        return "DC-" + datePart + "-" + String.format("%04d", seq);
    }
}
