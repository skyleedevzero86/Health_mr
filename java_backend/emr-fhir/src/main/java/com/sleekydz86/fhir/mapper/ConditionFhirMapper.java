package com.sleekydz86.fhir.mapper;

import com.sleekydz86.emrclinical.diagnosis.entity.DiagnosisCertificateEntity;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class ConditionFhirMapper {
    public static final String KCD_SYSTEM = "urn:oid:1.2.410.200001.1.2";

    public Condition toFhir(DiagnosisCertificateEntity source) {
        Condition target = new Condition();
        target.setId(String.valueOf(source.getCertificateId()));
        target.setClinicalStatus(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/condition-clinical", "active", "Active")));
        target.setVerificationStatus(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/condition-ver-status", "confirmed", "Confirmed")));
        target.setCode(new CodeableConcept(new Coding(KCD_SYSTEM, source.getKcdCode().getCode(), source.getDiagnosisName())));
        target.setSubject(new Reference("Patient/" + source.getPatient().getPatientNoValue()));
        target.setRecorder(new Reference("Practitioner/" + source.getDoctor().getId()));
        if (source.getDiagnosisDate() != null) target.setOnset(new DateTimeType(java.sql.Date.valueOf(source.getDiagnosisDate())));
        if (source.getClinicalFindings() != null) target.addNote().setText(source.getClinicalFindings());
        return target;
    }
}
