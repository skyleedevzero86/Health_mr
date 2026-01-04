package com.sleekydz86.support.examination.journal.service;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.support.examination.entity.ExaminationEntity;
import com.sleekydz86.support.examination.entity.ExaminationResultEntity;
import com.sleekydz86.support.examination.journal.dto.ExaminationResultRegisterRequest;
import com.sleekydz86.support.examination.journal.dto.ExaminationResultResponse;
import com.sleekydz86.support.examination.journal.dto.ExaminationResultUpdateRequest;
import com.sleekydz86.support.examination.journal.repository.ExaminationResultRepository;
import com.sleekydz86.support.examination.repository.ExaminationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExaminationResultService {

    private final ExaminationResultRepository resultRepository;
    private final ExaminationRepository examinationRepository;
    private final PatientRepository patientRepository;
    private final TreatmentRepository treatmentRepository;

    @Transactional
    public ExaminationResultResponse registerResult(ExaminationResultRegisterRequest request) {

        ExaminationEntity examination = examinationRepository.findByExaminationId(request.getExaminationId())
                .orElseThrow(() -> new IllegalArgumentException("검사 정보를 찾을 수 없습니다."));

        PatientEntity patient = patientRepository.findByPatientNo(request.getPatientNo())
                .orElseThrow(() -> new IllegalArgumentException("환자 정보를 찾을 수 없습니다."));

        TreatmentEntity treatment = treatmentRepository.findById(request.getTreatmentId())
                .orElseThrow(() -> new IllegalArgumentException("진료 정보를 찾을 수 없습니다."));

        ExaminationResultEntity result = ExaminationResultEntity.builder()
                .examinationEntity(examination)
                .patientEntity(patient)
                .treatmentEntity(treatment)
                .examinationDate(request.getExaminationDate())
                .examinationResult(request.getExaminationResult())
                .examinationNormal(request.getExaminationNormal())
                .examinationNotes(request.getExaminationNotes())
                .build();

        ExaminationResultEntity saved = resultRepository.save(result);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExaminationResultResponse getResult(Long resultId) {
        ExaminationResultEntity result = resultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("검사 결과를 찾을 수 없습니다."));
        return toResponse(result);
    }

    @Transactional(readOnly = true)
    public List<ExaminationResultResponse> getResultsByPatient(Long patientNo) {
        List<ExaminationResultEntity> results = resultRepository.findByPatientEntity_PatientNo(patientNo);
        return results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExaminationResultResponse> getResultsByExamination(Long examinationId) {
        List<ExaminationResultEntity> results = resultRepository.findByExaminationEntity_ExaminationId(examinationId);
        return results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExaminationResultResponse> getResultsByDate(LocalDate date) {
        List<ExaminationResultEntity> results = resultRepository.findByExaminationDate(date);
        return results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExaminationResultResponse updateResult(Long resultId, ExaminationResultUpdateRequest request) {
        ExaminationResultEntity result = resultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("검사 결과를 찾을 수 없습니다."));

        ExaminationResultEntity updated = ExaminationResultEntity.builder()
                .examinationResultId(result.getExaminationResultId())
                .examinationEntity(result.getExaminationEntity())
                .patientEntity(result.getPatientEntity())
                .treatmentEntity(result.getTreatmentEntity())
                .examinationDate(request.getExaminationDate() != null ? request.getExaminationDate() : result.getExaminationDate())
                .examinationResult(request.getExaminationResult() != null ? request.getExaminationResult() : result.getExaminationResult())
                .examinationNormal(request.getExaminationNormal() != null ? request.getExaminationNormal() : result.getExaminationNormal())
                .examinationNotes(request.getExaminationNotes() != null ? request.getExaminationNotes() : result.getExaminationNotes())
                .build();

        ExaminationResultEntity saved = resultRepository.save(updated);
        return toResponse(saved);
    }

    @Transactional
    public ExaminationResultResponse deleteResult(Long resultId) {
        ExaminationResultEntity result = resultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("검사 결과를 찾을 수 없습니다."));

        ExaminationResultResponse response = toResponse(result);
        resultRepository.delete(result);
        return response;
    }

    private ExaminationResultResponse toResponse(ExaminationResultEntity result) {
        ExaminationEntity examination = result.getExaminationEntity();
        PatientEntity patient = result.getPatientEntity();
        TreatmentEntity treatment = result.getTreatmentEntity();

        return ExaminationResultResponse.builder()
                .examinationResultId(result.getExaminationResultId())
                .examinationId(examination.getExaminationId())
                .examinationName(examination.getExaminationName())
                .patientNo(patient.getPatientNo().getValue())
                .patientName(patient.getPatientName())
                .treatmentId(treatment.getTreatmentId())
                .examinationDate(result.getExaminationDate())
                .examinationResult(result.getExaminationResult())
                .examinationNormal(result.getExaminationNormal())
                .examinationNotes(result.getExaminationNotes())
                .build();
    }
}

