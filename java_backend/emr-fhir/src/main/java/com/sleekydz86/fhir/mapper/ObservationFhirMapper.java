package com.sleekydz86.fhir.mapper;

import com.sleekydz86.support.examination.entity.ExaminationResultEntity;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ObservationFhirMapper {
    public static final String LOINC_SYSTEM = "http://loinc.org";
    private static final Map<String, String> LOINC_CODES = Map.of(
            "혈당", "2345-7", "공복혈당", "1558-6", "혈색소", "718-7",
            "수축기혈압", "8480-6", "이완기혈압", "8462-4", "체온", "8310-5"
    );

    public Observation toFhir(ExaminationResultEntity source) {
        Observation target = new Observation();
        target.setId(String.valueOf(source.getExaminationResultId()));
        target.setStatus(Observation.ObservationStatus.FINAL);
        String name = source.getExaminationEntity().getExaminationName();
        CodeableConcept concept = new CodeableConcept().setText(name);
        String code = findLoinc(name);
        if (code != null) concept.addCoding(new Coding(LOINC_SYSTEM, code, name));
        target.setCode(concept);
        target.setSubject(new Reference("Patient/" + source.getPatientEntity().getPatientNoValue()));
        target.setEncounter(new Reference("Encounter/" + source.getTreatmentEntity().getTreatmentId()));
        if (source.getExaminationDate() != null) target.setEffective(new DateTimeType(java.sql.Date.valueOf(source.getExaminationDate())));
        if (source.getExaminationResult() != null) target.setValue(new StringType(source.getExaminationResult()));
        if (source.getExaminationNormal() != null) target.addInterpretation(new CodeableConcept(new Coding(
                "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
                source.getExaminationNormal() ? "N" : "A", null)));
        if (source.getExaminationNotes() != null) target.addNote().setText(source.getExaminationNotes());
        return target;
    }

    private String findLoinc(String name) {
        if (name == null) return null;
        String exact = LOINC_CODES.get(name);
        if (exact != null) return exact;
        return LOINC_CODES.entrySet().stream()
                .filter(e -> name.contains(e.getKey()))
                .max(java.util.Comparator.comparingInt(e -> e.getKey().length()))
                .map(Map.Entry::getValue)
                .orElse(null);
    }
}
