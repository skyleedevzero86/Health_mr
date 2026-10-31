package com.sleekydz86.fhir.service;

import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.emrclinical.diagnosis.repository.DiagnosisCertificateRepository;
import com.sleekydz86.emrclinical.prescription.repository.PrescriptionItemRepository;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.fhir.exception.FhirResourceNotFoundException;
import com.sleekydz86.fhir.exception.InvalidFhirSearchException;
import com.sleekydz86.fhir.mapper.*;
import com.sleekydz86.support.examination.journal.repository.ExaminationResultRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
public class FhirReadService {
    private final PatientRepository patients;
    private final UserRepository users;
    private final TreatmentRepository treatments;
    private final ExaminationResultRepository observations;
    private final DiagnosisCertificateRepository conditions;
    private final PrescriptionItemRepository medicationRequests;
    private final PatientFhirMapper patientMapper;
    private final PractitionerFhirMapper practitionerMapper;
    private final EncounterFhirMapper encounterMapper;
    private final ObservationFhirMapper observationMapper;
    private final ConditionFhirMapper conditionMapper;
    private final MedicationRequestFhirMapper medicationMapper;

    public FhirReadService(PatientRepository patients, UserRepository users, TreatmentRepository treatments,
                           ExaminationResultRepository observations, DiagnosisCertificateRepository conditions,
                           PrescriptionItemRepository medicationRequests, PatientFhirMapper patientMapper,
                           PractitionerFhirMapper practitionerMapper, EncounterFhirMapper encounterMapper,
                           ObservationFhirMapper observationMapper, ConditionFhirMapper conditionMapper,
                           MedicationRequestFhirMapper medicationMapper) {
        this.patients = patients;
        this.users = users;
        this.treatments = treatments;
        this.observations = observations;
        this.conditions = conditions;
        this.medicationRequests = medicationRequests;
        this.patientMapper = patientMapper;
        this.practitionerMapper = practitionerMapper;
        this.encounterMapper = encounterMapper;
        this.observationMapper = observationMapper;
        this.conditionMapper = conditionMapper;
        this.medicationMapper = medicationMapper;
    }

    public Patient patient(Long id) {
        return patientMapper.toFhir(patients.findByPatientNo(id).orElseThrow(() -> new FhirResourceNotFoundException("Patient", id)));
    }

    public Practitioner practitioner(Long id) {
        return practitionerMapper.toFhir(users.findById(id).orElseThrow(() -> new FhirResourceNotFoundException("Practitioner", id)));
    }

    public Encounter encounter(Long id) {
        return encounterMapper.toFhir(treatments.findById(id).orElseThrow(() -> new FhirResourceNotFoundException("Encounter", id)));
    }

    public Observation observation(Long id) {
        return observationMapper.toFhir(observations.findById(id).orElseThrow(() -> new FhirResourceNotFoundException("Observation", id)));
    }

    public Condition condition(Long id) {
        return conditionMapper.toFhir(conditions.findById(id).orElseThrow(() -> new FhirResourceNotFoundException("Condition", id)));
    }

    public MedicationRequest medicationRequest(Long id) {
        return medicationMapper.toFhir(medicationRequests.findById(id).orElseThrow(() -> new FhirResourceNotFoundException("MedicationRequest", id)));
    }

    public Bundle patients(String name) {
        var sources = name == null || name.isBlank()
                ? patients.findAll()
                : patients.findByPatientNameContaining(name);
        return bundle("Patient", sources, patientMapper::toFhir);
    }

    public Bundle practitioners(String name) {
        var sources = users.findByRoleIn(List.of(RoleType.DOCTOR, RoleType.NURSE)).stream()
                .filter(user -> name == null || name.isBlank()
                        || user.getName().toLowerCase(java.util.Locale.ROOT)
                        .contains(name.toLowerCase(java.util.Locale.ROOT)))
                .toList();
        return bundle("Practitioner", sources, practitionerMapper::toFhir);
    }

    public Bundle encounters(String subject) {
        Long patientNo = patientNo(subject);
        return bundle("Encounter", treatments.findByPatientEntity_PatientNo(patientNo), encounterMapper::toFhir);
    }

    public Bundle observations(String subject, String code) {
        Long patientNo = patientNo(subject);
        List<Observation> resources = observations.findByPatientEntity_PatientNo(patientNo).stream()
                .map(observationMapper::toFhir)
                .filter(item -> matchesCode(item.getCode(), code))
                .toList();
        return bundleResources("Observation", resources);
    }

    public Bundle conditions(String subject) {
        Long patientNo = patientNo(subject);
        return bundle("Condition", conditions.findByPatientNo(patientNo), conditionMapper::toFhir);
    }

    public Bundle medicationRequests(String subject) {
        Long patientNo = patientNo(subject);
        return bundle("MedicationRequest", medicationRequests.findByPrescriptionEntity_PatientEntity_PatientNo(patientNo), medicationMapper::toFhir);
    }

    private Long patientNo(String subject) {
        if (subject == null || !subject.matches("Patient/\\d+")) {
            throw new InvalidFhirSearchException("subject must use the form Patient/{id}");
        }
        return Long.valueOf(subject.substring("Patient/".length()));
    }

    private boolean matchesCode(CodeableConcept concept, String requested) {
        if (requested == null || requested.isBlank()) return true;
        String code = requested.contains("|") ? requested.substring(requested.lastIndexOf('|') + 1) : requested;
        return concept.getCoding().stream().anyMatch(coding -> code.equals(coding.getCode()));
    }

    private <T, R extends Resource> Bundle bundle(String type, List<T> sources, Function<T, R> mapper) {
        return bundleResources(type, sources.stream().map(mapper).toList());
    }

    private Bundle bundleResources(String type, List<? extends Resource> resources) {
        Bundle bundle = new Bundle().setType(Bundle.BundleType.SEARCHSET).setTotal(resources.size());
        resources.forEach(resource -> bundle.addEntry().setFullUrl(type + "/" + resource.getIdElement().getIdPart()).setResource(resource));
        return bundle;
    }
}
