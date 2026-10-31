package com.sleekydz86.domain.patient.service;

import com.sleekydz86.core.security.encryption.BlindIndexService;
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
    private final BlindIndexService blindIndexService;

    public List<PatientEntity> checkDuplicateByRrn(String patientRrn) {
        String rrnHmac = blindIndexService.hmacRrn(patientRrn);
        if (rrnHmac == null) {
            return List.of();
        }
        return patientRepository.findByRrnHmac(rrnHmac);
    }

    public List<PatientEntity> checkDuplicateByEmail(String email) {
        String emailHmac = blindIndexService.hmacEmail(email);
        if (emailHmac == null) {
            return List.of();
        }
        return patientRepository.findByEmailHmac(emailHmac);
    }

    public List<PatientEntity> checkDuplicateByTel(String tel) {
        String telHmac = blindIndexService.hmacPhone(tel);
        if (telHmac == null) {
            return List.of();
        }
        return patientRepository.findByTelHmac(telHmac);
    }

    public List<PatientEntity> checkDuplicate(String patientRrn, String email, String tel) {
        if (patientRrn != null) {
            List<PatientEntity> byRrn = checkDuplicateByRrn(patientRrn);
            if (!byRrn.isEmpty()) {
                return byRrn;
            }
        }
        if (email != null) {
            List<PatientEntity> byEmail = checkDuplicateByEmail(email);
            if (!byEmail.isEmpty()) {
                return byEmail;
            }
        }
        if (tel != null) {
            List<PatientEntity> byTel = checkDuplicateByTel(tel);
            if (!byTel.isEmpty()) {
                return byTel;
            }
        }
        return List.of();
    }
}
