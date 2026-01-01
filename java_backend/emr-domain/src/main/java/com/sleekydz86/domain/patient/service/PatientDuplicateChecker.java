package com.sleekydz86.domain.patient.service;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientDuplicateChecker {

    private final PatientRepository patientRepository;

    public List<PatientEntity> checkDuplicateByRrn(String patientRrn) {
        return patientRepository.findByPatientRrn(patientRrn)
                .map(List::of)
                .orElse(List.of());
    }

    public List<PatientEntity> checkDuplicateByEmail(String email) {
        return patientRepository.findByPatientEmail(email)
                .map(List::of)
                .orElse(List.of());
    }

    public List<PatientEntity> checkDuplicateByTel(String tel) {
        return patientRepository.findByPatientTel(tel)
                .map(List::of)
                .orElse(List.of());
    }

    public List<PatientEntity> checkDuplicate(String patientRrn, String email, String tel) {

        if (patientRrn != null && patientRepository.existsByPatientRrn(patientRrn)) {
            return checkDuplicateByRrn(patientRrn);
        }
        if (email != null && patientRepository.existsByPatientEmail(email)) {
            return checkDuplicateByEmail(email);
        }
        if (tel != null && patientRepository.existsByPatientTel(tel)) {
            return checkDuplicateByTel(tel);
        }
        return List.of();
    }
}

