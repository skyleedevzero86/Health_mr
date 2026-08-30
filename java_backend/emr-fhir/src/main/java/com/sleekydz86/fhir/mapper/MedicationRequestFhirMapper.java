package com.sleekydz86.fhir.mapper;

import com.sleekydz86.emrclinical.prescription.entity.PrescriptionItemEntity;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MedicationRequestFhirMapper {
    public static final String DRUG_CODE_SYSTEM = "urn:oid:kr-emr:drug-code";

    public MedicationRequest toFhir(PrescriptionItemEntity source) {
        var prescription = source.getPrescriptionEntity();
        MedicationRequest target = new MedicationRequest();
        target.setId(String.valueOf(source.getPrescriptionItemId()));
        target.setStatus(switch (prescription.getPrescriptionStatus()) {
            case PENDING -> MedicationRequest.MedicationRequestStatus.DRAFT;
            case PRESCRIBED -> MedicationRequest.MedicationRequestStatus.ACTIVE;
            case DISPENSED -> MedicationRequest.MedicationRequestStatus.COMPLETED;
            case CANCELLED -> MedicationRequest.MedicationRequestStatus.CANCELLED;
        });
        target.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
        target.setMedication(new CodeableConcept(new Coding(DRUG_CODE_SYSTEM, source.getDrugCode(), source.getDrugName())));
        target.setSubject(new Reference("Patient/" + prescription.getPatientEntity().getPatientNoValue()));
        target.setEncounter(new Reference("Encounter/" + prescription.getTreatmentEntity().getTreatmentId()));
        target.setRequester(new Reference("Practitioner/" + prescription.getPrescriptionDoc().getId()));
        if (prescription.getPrescriptionDate() != null) target.setAuthoredOn(java.util.Date.from(
                prescription.getPrescriptionDate().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        Dosage dosage = target.addDosageInstruction().setText(source.getDosage());
        dosage.setTiming(new Timing().setRepeat(new Timing.TimingRepeatComponent()
                .setFrequency(source.getFrequency()).setPeriod(1).setPeriodUnit(Timing.UnitsOfTime.D)));
        dosage.addDoseAndRate().setDose(new StringType(source.getDose()));
        target.getDispenseRequest()
                .setQuantity(new Quantity(source.getTotalQuantity()).setUnit(source.getUnit()))
                .setExpectedSupplyDuration(new Duration().setValue(source.getDays()).setUnit("days")
                        .setSystem("http://unitsofmeasure.org").setCode("d"));
        return target;
    }
}
