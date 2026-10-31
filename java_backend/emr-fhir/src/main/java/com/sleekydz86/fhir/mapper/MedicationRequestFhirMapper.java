package com.sleekydz86.fhir.mapper;

import com.sleekydz86.emrclinical.prescription.entity.PrescriptionItemEntity;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MedicationRequestFhirMapper {
    public static final String DRUG_CODE_SYSTEM = "urn:oid:kr-emr:drug-code";
    private static final Pattern DOSE_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*([A-Za-z가-힣μµ]+)?$");

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
        if (prescription.getPrescriptionDate() != null) target.setAuthoredOn(Date.from(
                prescription.getPrescriptionDate().atZone(ZoneId.systemDefault()).toInstant()));
        Dosage dosage = target.addDosageInstruction();
        SimpleQuantity parsedDose = parseDose(source.getDose(), source.getUnit());
        if (parsedDose != null) {
            dosage.setText(source.getDosage());
            dosage.addDoseAndRate().setDose(parsedDose);
        } else {
            dosage.setText(source.getDose());
        }
        dosage.setTiming(new Timing().setRepeat(new Timing.TimingRepeatComponent()
                .setFrequency(source.getFrequency()).setPeriod(1).setPeriodUnit(Timing.UnitsOfTime.D)));
        Duration supplyDuration = new Duration();
        supplyDuration.setValue(source.getDays());
        supplyDuration.setUnit("days");
        supplyDuration.setSystem("http://unitsofmeasure.org");
        supplyDuration.setCode("d");
        target.getDispenseRequest()
                .setQuantity(new Quantity(source.getTotalQuantity()).setUnit(source.getUnit()))
                .setExpectedSupplyDuration(supplyDuration);
        return target;
    }

    private SimpleQuantity parseDose(String dose, String fallbackUnit) {
        if (dose == null || dose.isBlank()) {
            return null;
        }
        Matcher matcher = DOSE_PATTERN.matcher(dose.trim());
        if (!matcher.matches()) {
            return null;
        }
        SimpleQuantity quantity = new SimpleQuantity();
        quantity.setValue(new BigDecimal(matcher.group(1)));
        String unit = matcher.group(2) == null ? "" : matcher.group(2).trim();
        if (unit.isEmpty() && fallbackUnit != null && !fallbackUnit.isBlank()) {
            unit = fallbackUnit;
        }
        if (!unit.isEmpty()) {
            quantity.setUnit(unit);
        }
        return quantity;
    }
}
