package com.sleekydz86.fhir.service;

import com.sleekydz86.domain.common.valueobject.PatientNumber;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.emrclinical.diagnosis.repository.DiagnosisCertificateRepository;
import com.sleekydz86.emrclinical.prescription.repository.PrescriptionItemRepository;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.fhir.mapper.*;
import com.sleekydz86.support.examination.journal.repository.ExaminationResultRepository;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FhirReadServiceTest {
    @Test
    void patientAndPractitionerSearchReturnSearchsetBundles() {
        PatientRepository patients = mock(PatientRepository.class);
        UserRepository users = mock(UserRepository.class);
        PatientEntity patient = PatientEntity.builder().patientNo(PatientNumber.of(123L))
                .patientName("홍길동").patientGender("MALE").build();
        UserEntity doctor = UserEntity.builder().id(7L).role(RoleType.DOCTOR).name("김의사").build();
        when(patients.findByPatientNameContaining("홍")).thenReturn(List.of(patient));
        when(users.findByRoleIn(List.of(RoleType.DOCTOR, RoleType.NURSE))).thenReturn(List.of(doctor));

        FhirReadService service = new FhirReadService(
                patients, users, mock(TreatmentRepository.class), mock(ExaminationResultRepository.class),
                mock(DiagnosisCertificateRepository.class), mock(PrescriptionItemRepository.class),
                new PatientFhirMapper(), new PractitionerFhirMapper(), mock(EncounterFhirMapper.class),
                mock(ObservationFhirMapper.class), mock(ConditionFhirMapper.class),
                mock(MedicationRequestFhirMapper.class));

        Bundle patientBundle = service.patients("홍");
        Bundle practitionerBundle = service.practitioners("김");

        assertThat(patientBundle.getType()).isEqualTo(Bundle.BundleType.SEARCHSET);
        assertThat(patientBundle.getTotal()).isEqualTo(1);
        assertThat(patientBundle.getEntryFirstRep().getResource().fhirType()).isEqualTo("Patient");
        assertThat(practitionerBundle.getType()).isEqualTo(Bundle.BundleType.SEARCHSET);
        assertThat(practitionerBundle.getTotal()).isEqualTo(1);
        assertThat(practitionerBundle.getEntryFirstRep().getResource().fhirType()).isEqualTo("Practitioner");
    }
}
