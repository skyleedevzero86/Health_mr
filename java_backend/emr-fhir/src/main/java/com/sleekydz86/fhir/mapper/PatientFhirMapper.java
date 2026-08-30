package com.sleekydz86.fhir.mapper;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class PatientFhirMapper {
    public Patient toFhir(PatientEntity source) {
        Patient target = new Patient();
        target.setId(String.valueOf(source.getPatientNoValue()));
        target.addIdentifier().setSystem("urn:oid:kr-emr:patient-number").setValue(String.valueOf(source.getPatientNoValue()));
        target.addName().setText(source.getPatientName());
        target.setGender(toGender(source.getPatientGender()));
        if (source.getPatientBirth() != null) target.setBirthDate(java.sql.Date.valueOf(source.getPatientBirth()));
        if (source.getPatientAddress() != null) target.addAddress(new Address().setText(source.getPatientAddress()));
        if (source.getPatientTelValue() != null) target.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.PHONE).setValue(source.getPatientTelValue()));
        if (source.getPatientEmailValue() != null) target.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue(source.getPatientEmailValue()));
        return target;
    }

    private Enumerations.AdministrativeGender toGender(String gender) {
        if (gender == null) return Enumerations.AdministrativeGender.UNKNOWN;
        return switch (gender.toUpperCase()) {
            case "MALE", "M", "남", "남성" -> Enumerations.AdministrativeGender.MALE;
            case "FEMALE", "F", "여", "여성" -> Enumerations.AdministrativeGender.FEMALE;
            case "OTHER" -> Enumerations.AdministrativeGender.OTHER;
            default -> Enumerations.AdministrativeGender.UNKNOWN;
        };
    }
}
