package com.sleekydz86.support.examination.journal.service;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.support.examination.entity.ExaminationEntity;
import com.sleekydz86.support.examination.entity.ExaminationScheduleEntity;
import com.sleekydz86.support.examination.journal.dto.ExaminationScheduleRegisterRequest;
import com.sleekydz86.support.examination.journal.dto.ExaminationScheduleResponse;
import com.sleekydz86.support.examination.journal.dto.ExaminationScheduleUpdateRequest;
import com.sleekydz86.support.examination.journal.repository.ExaminationScheduleRepository;
import com.sleekydz86.support.examination.repository.ExaminationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExaminationScheduleService {

    private final ExaminationScheduleRepository scheduleRepository;
    private final ExaminationRepository examinationRepository;
    private final PatientRepository patientRepository;
    private final TreatmentRepository treatmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExaminationScheduleResponse registerSchedule(ExaminationScheduleRegisterRequest request) {

        ExaminationEntity examination = examinationRepository.findByExaminationId(request.getExaminationId())
                .orElseThrow(() -> new IllegalArgumentException("검사 정보를 찾을 수 없습니다."));

        PatientEntity patient = patientRepository.findByPatientNo(request.getPatientNo())
                .orElseThrow(() -> new IllegalArgumentException("환자 정보를 찾을 수 없습니다."));

        TreatmentEntity treatment = treatmentRepository.findById(request.getTreatmentId())
                .orElseThrow(() -> new IllegalArgumentException("진료 정보를 찾을 수 없습니다."));

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        ExaminationScheduleEntity schedule = ExaminationScheduleEntity.builder()
                .examinationEntity(examination)
                .patientEntity(patient)
                .treatmentEntity(treatment)
                .userEntity(user)
                .examinationDate(request.getExaminationDate())
                .build();

        ExaminationScheduleEntity saved = scheduleRepository.save(schedule);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExaminationScheduleResponse getSchedule(Long scheduleId) {
        ExaminationScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("검사 일정을 찾을 수 없습니다."));
        return toResponse(schedule);
    }

    @Transactional(readOnly = true)
    public List<ExaminationScheduleResponse> getSchedulesByPatient(Long patientNo) {
        List<ExaminationScheduleEntity> schedules = scheduleRepository.findByPatientEntity_PatientNo(patientNo);
        return schedules.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExaminationScheduleResponse> getSchedulesByExamination(Long examinationId) {
        List<ExaminationScheduleEntity> schedules = scheduleRepository.findByExaminationEntity_ExaminationId(examinationId);
        return schedules.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExaminationScheduleResponse> getSchedulesByDate(LocalDate date) {
        List<ExaminationScheduleEntity> schedules = scheduleRepository.findByExaminationDate(date);
        return schedules.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExaminationScheduleResponse updateSchedule(Long scheduleId, ExaminationScheduleUpdateRequest request) {
        ExaminationScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("검사 일정을 찾을 수 없습니다."));

        ExaminationScheduleEntity updated = schedule.toBuilder()
                .examinationDate(request.getExaminationDate() != null ? request.getExaminationDate() : schedule.getExaminationDate())
                .build();

        ExaminationScheduleEntity saved = scheduleRepository.save(updated);
        return toResponse(saved);
    }

    @Transactional
    public ExaminationScheduleResponse deleteSchedule(Long scheduleId) {
        ExaminationScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("검사 일정을 찾을 수 없습니다."));

        ExaminationScheduleResponse response = toResponse(schedule);
        scheduleRepository.delete(schedule);
        return response;
    }

    private ExaminationScheduleResponse toResponse(ExaminationScheduleEntity schedule) {
        ExaminationEntity examination = schedule.getExaminationEntity();
        PatientEntity patient = schedule.getPatientEntity();
        TreatmentEntity treatment = schedule.getTreatmentEntity();
        UserEntity user = schedule.getUserEntity();

        return ExaminationScheduleResponse.builder()
                .examinationScheduleId(schedule.getExaminationScheduleId())
                .examinationId(examination.getExaminationId())
                .examinationName(examination.getExaminationName())
                .patientNo(patient.getPatientNo())
                .patientName(patient.getPatientName())
                .treatmentId(treatment.getTreatmentId())
                .userId(user.getId())
                .userName(user.getName())
                .examinationDate(schedule.getExaminationDate())
                .build();
    }
}

