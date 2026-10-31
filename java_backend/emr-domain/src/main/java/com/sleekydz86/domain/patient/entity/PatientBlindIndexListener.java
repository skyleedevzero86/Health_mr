package com.sleekydz86.domain.patient.entity;

import com.sleekydz86.core.security.encryption.BlindIndexService;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class PatientBlindIndexListener {

    @PrePersist
    @PreUpdate
    public void apply(PatientEntity patient) {
        BlindIndexService service = BlindIndexService.current();
        patient.applyBlindIndexes(
                service.hmacRrn(patient.getPatientRrnValue()),
                service.hmacPhone(patient.getPatientTelValue()),
                service.hmacEmail(patient.getPatientEmailValue()),
                service.hmacPassport(patient.getPatientPassport()));
    }
}
