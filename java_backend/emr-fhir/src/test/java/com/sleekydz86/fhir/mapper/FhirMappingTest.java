package com.sleekydz86.fhir.mapper;

import ca.uhn.fhir.context.FhirContext;
import com.sleekydz86.domain.common.valueobject.PatientNumber;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.emrclinical.prescription.entity.PrescriptionEntity;
import com.sleekydz86.emrclinical.prescription.entity.PrescriptionItemEntity;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.types.*;
import com.sleekydz86.support.examination.entity.ExaminationEntity;
import com.sleekydz86.support.examination.entity.ExaminationResultEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FhirMappingTest {
    private final PatientEntity patient = PatientEntity.builder()
            .patientNo(PatientNumber.of(123L)).patientName("홍길동").patientGender("MALE").build();
    private final UserEntity doctor = UserEntity.builder().id(7L).role(RoleType.DOCTOR).name("김의사").build();
    private final TreatmentEntity treatment = TreatmentEntity.builder().treatmentId(45L).patientEntity(patient)
            .treatmentDoc(doctor).treatmentType(TreatmentType.OUTPATIENT).treatmentStatus(TreatmentStatus.COMPLETED).build();

    @Test
    void observationKeepsPatientEncounterGraphAndLoincCode() {
        ExaminationEntity examination = ExaminationEntity.builder().examinationId(9L).examinationName("공복혈당")
                .examinationType("LAB").examinationLocation("검사실").examinationPrice(1000L).build();
        ExaminationResultEntity result = ExaminationResultEntity.builder().examinationResultId(88L)
                .examinationEntity(examination).patientEntity(patient).treatmentEntity(treatment)
                .examinationResult("95 mg/dL").examinationNormal(true).build();

        var observation = new ObservationFhirMapper().toFhir(result);

        assertThat(observation.getSubject().getReference()).isEqualTo("Patient/123");
        assertThat(observation.getEncounter().getReference()).isEqualTo("Encounter/45");
        assertThat(observation.getCode().getCodingFirstRep().getSystem()).isEqualTo("http://loinc.org");
        assertThat(observation.getCode().getCodingFirstRep().getCode()).isEqualTo("1558-6");
        assertThat(FhirContext.forR4Cached().newJsonParser().encodeResourceToString(observation))
                .contains("\"resourceType\":\"Observation\"");
    }

    @Test
    void onePrescriptionItemBecomesOneMedicationRequest() {
        PrescriptionEntity prescription = PrescriptionEntity.builder().prescriptionId(12L).patientEntity(patient)
                .treatmentEntity(treatment).prescriptionDoc(doctor).prescriptionType(PrescriptionType.OUTPATIENT)
                .prescriptionStatus(PrescriptionStatus.PRESCRIBED).build();
        PrescriptionItemEntity item = PrescriptionItemEntity.builder().prescriptionItemId(99L)
                .prescriptionEntity(prescription).drugCode("DRUG-001").drugName("샘플약")
                .dosage("식후 복용").dose("1정").frequency(3).days(5).totalQuantity(15).unit("정").build();

        var request = new MedicationRequestFhirMapper().toFhir(item);

        assertThat(request.getIdElement().getIdPart()).isEqualTo("99");
        assertThat(request.getSubject().getReference()).isEqualTo("Patient/123");
        assertThat(request.getEncounter().getReference()).isEqualTo("Encounter/45");
        assertThat(request.getMedicationCodeableConcept().getCodingFirstRep().getCode()).isEqualTo("DRUG-001");
    }
}
