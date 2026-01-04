package com.sleekydz86.support.examination.journal.service;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.support.equipment.entity.EquipmentEntity;
import com.sleekydz86.support.equipment.repository.EquipmentRepository;
import com.sleekydz86.support.examination.entity.ExaminationEntity;
import com.sleekydz86.support.examination.entity.ExaminationJournalEntity;
import com.sleekydz86.support.examination.journal.dto.ExaminationJournalRegisterRequest;
import com.sleekydz86.support.examination.journal.dto.ExaminationJournalResponse;
import com.sleekydz86.support.examination.journal.dto.ExaminationJournalUpdateRequest;
import com.sleekydz86.support.examination.repository.ExaminationJournalRepository;
import com.sleekydz86.support.examination.repository.ExaminationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ExaminationJournalService {

    private final ExaminationJournalRepository journalRepository;
    private final ExaminationRepository examinationRepository;
    private final PatientRepository patientRepository;
    private final TreatmentRepository treatmentRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional
    public ExaminationJournalResponse registerJournal(ExaminationJournalRegisterRequest request) {

        ExaminationEntity examination = examinationRepository.findByExaminationId(request.getExaminationId())
                .orElseThrow(() -> new IllegalArgumentException("검사 정보를 찾을 수 없습니다."));


        PatientEntity patient = patientRepository.findByPatientNo(request.getPatientNo())
                .orElseThrow(() -> new IllegalArgumentException("환자 정보를 찾을 수 없습니다."));

        TreatmentEntity treatment = treatmentRepository.findById(request.getTreatmentId())
                .orElseThrow(() -> new IllegalArgumentException("진료 정보를 찾을 수 없습니다."));

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        EquipmentEntity equipment = equipmentRepository.findByEquipmentId(request.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException("장비 정보를 찾을 수 없습니다."));

        ExaminationJournalEntity journal = ExaminationJournalEntity.builder()
                .examinationEntity(examination)
                .patientEntity(patient)
                .treatmentEntity(treatment)
                .userEntity(user)
                .equipmentEntity(equipment)
                .examinationTime(request.getExaminationTime())
                .examinationEquipmentUsage(request.getExaminationEquipmentUsage())
                .examinationNotes(request.getExaminationNotes())
                .build();

        ExaminationJournalEntity saved = journalRepository.save(journal);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExaminationJournalResponse getJournal(Long journalId) {
        ExaminationJournalEntity journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new IllegalArgumentException("검사 일지를 찾을 수 없습니다."));
        return toResponse(journal);
    }

    @Transactional(readOnly = true)
    public List<ExaminationJournalResponse> getJournalsByPatient(Long patientNo) {
        List<ExaminationJournalEntity> journals = journalRepository.findByPatientEntity_PatientNo(patientNo);
        return journals.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExaminationJournalResponse> getJournalsByExamination(Long examinationId) {
        List<ExaminationJournalEntity> journals = journalRepository.findByExaminationEntity_ExaminationId(examinationId);
        return journals.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExaminationJournalResponse> getJournalsByTreatment(Long treatmentId) {
        List<ExaminationJournalEntity> journals = journalRepository.findByTreatmentEntity_TreatmentId(treatmentId);
        return journals.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExaminationJournalResponse updateJournal(Long journalId, ExaminationJournalUpdateRequest request) {
        ExaminationJournalEntity journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new IllegalArgumentException("검사 일지를 찾을 수 없습니다."));

        ExaminationJournalEntity updated = journal.toBuilder()
                .examinationTime(request.getExaminationTime() != null ? request.getExaminationTime() : journal.getExaminationTime())
                .examinationEquipmentUsage(request.getExaminationEquipmentUsage() != null ? request.getExaminationEquipmentUsage() : journal.getExaminationEquipmentUsage())
                .examinationNotes(request.getExaminationNotes() != null ? request.getExaminationNotes() : journal.getExaminationNotes())
                .build();

        ExaminationJournalEntity saved = journalRepository.save(updated);
        return toResponse(saved);
    }

    @Transactional
    public ExaminationJournalResponse deleteJournal(Long journalId) {
        ExaminationJournalEntity journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new IllegalArgumentException("검사 일지를 찾을 수 없습니다."));

        ExaminationJournalResponse response = toResponse(journal);
        journalRepository.delete(journal);
        return response;
    }

    private ExaminationJournalResponse toResponse(ExaminationJournalEntity journal) {
        ExaminationEntity examination = journal.getExaminationEntity();
        PatientEntity patient = journal.getPatientEntity();
        TreatmentEntity treatment = journal.getTreatmentEntity();
        UserEntity user = journal.getUserEntity();
        EquipmentEntity equipment = journal.getEquipmentEntity();

        return ExaminationJournalResponse.builder()
                .examinationJournalId(journal.getExaminationJournalId())
                .examinationId(examination.getExaminationId())
                .examinationName(examination.getExaminationName())
                .patientNo(patient.getPatientNo())
                .patientName(patient.getPatientName())
                .treatmentId(treatment.getTreatmentId())
                .userId(user.getId())
                .userName(user.getName())
                .equipmentId(equipment.getEquipmentId())
                .equipmentName(equipment.getEquipmentName())
                .examinationTime(journal.getExaminationTime())
                .examinationEquipmentUsage(journal.getExaminationEquipmentUsage())
                .examinationNotes(journal.getExaminationNotes())
                .build();
    }
}

