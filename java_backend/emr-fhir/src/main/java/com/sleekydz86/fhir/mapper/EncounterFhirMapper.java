package com.sleekydz86.fhir.mapper;

import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;

@Component
public class EncounterFhirMapper {
    public Encounter toFhir(TreatmentEntity source) {
        Encounter target = new Encounter();
        target.setId(String.valueOf(source.getTreatmentId()));
        target.setStatus(switch (source.getTreatmentStatus()) {
            case PENDING -> Encounter.EncounterStatus.PLANNED;
            case IN_PROGRESS -> Encounter.EncounterStatus.INPROGRESS;
            case COMPLETED -> Encounter.EncounterStatus.FINISHED;
            case CANCELLED -> Encounter.EncounterStatus.CANCELLED;
        });
        target.setClass_(new Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", switch (source.getTreatmentType()) {
            case OUTPATIENT -> "AMB";
            case INPATIENT -> "IMP";
            case EMERGENCY -> "EMER";
        }, source.getTreatmentType().name()));
        target.setSubject(new Reference("Patient/" + source.getPatientEntity().getPatientNoValue()));
        target.addParticipant().setIndividual(new Reference("Practitioner/" + source.getTreatmentDoc().getId()));
        Period period = new Period();
        if (source.getTreatmentStartTime() != null) period.setStart(date(source.getTreatmentStartTime()));
        else if (source.getTreatmentDate() != null) period.setStart(date(source.getTreatmentDate()));
        if (source.getTreatmentEndTime() != null) period.setEnd(date(source.getTreatmentEndTime()));
        target.setPeriod(period);
        return target;
    }

    private Date date(java.time.LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }
}
