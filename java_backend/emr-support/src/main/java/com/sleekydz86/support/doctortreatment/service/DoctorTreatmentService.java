package com.sleekydz86.support.doctortreatment.service;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.support.doctortreatment.dto.*;
import com.sleekydz86.support.doctortreatment.entity.DoctorTreatmentEntity;
import com.sleekydz86.support.doctortreatment.repository.DoctorTreatmentRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorTreatmentService {

    private final DoctorTreatmentRepository doctorTreatmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Transactional
    public DoctorTreatmentResponse registerDoctorTreatment(DoctorTreatmentRegisterRequest request) {

        if (request.getDoctorTreatmentEnd().isBefore(request.getDoctorTreatmentStart()) ||
                request.getDoctorTreatmentEnd().isEqual(request.getDoctorTreatmentStart())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }

        PatientEntity patient = patientRepository.findByPatientNo(request.getPatientNo())
                .orElseThrow(() -> new RuntimeException("환자를 찾을 수 없습니다."));

        UserEntity doctor = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("의사를 찾을 수 없습니다."));

        DoctorTreatmentEntity entity = DoctorTreatmentEntity.builder()
                .patientEntity(patient)
                .userEntity(doctor)
                .doctorTreatmentStart(request.getDoctorTreatmentStart())
                .doctorTreatmentEnd(request.getDoctorTreatmentEnd())
                .build();

        DoctorTreatmentEntity saved = doctorTreatmentRepository.save(entity);
        return toResponse(saved);
    }

    public DoctorTreatmentResponse getDoctorTreatment(Long doctorTreatmentId) {
        DoctorTreatmentEntity entity = doctorTreatmentRepository.findByDoctorTreatmentId(doctorTreatmentId)
                .orElseThrow(() -> new RuntimeException("의사 진료 정보를 찾을 수 없습니다."));
        return toResponse(entity);
    }

    public List<DoctorTreatmentResponse> getDoctorTreatmentsByDoctor(Long userId) {
        List<DoctorTreatmentEntity> entities = doctorTreatmentRepository.findByUserEntity_Id(userId);
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<DoctorTreatmentResponse> getDoctorTreatmentsByPatient(Long patientNo) {
        List<DoctorTreatmentEntity> entities = doctorTreatmentRepository.findByPatientEntity_PatientNo(patientNo);
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<DoctorTreatmentResponse> getDoctorTreatmentsByPeriod(
            Long userId, LocalDateTime start, LocalDateTime end) {
        List<DoctorTreatmentEntity> entities = doctorTreatmentRepository
                .findByUserEntity_IdAndDoctorTreatmentStartBetween(userId, start, end);
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorTreatmentResponse updateDoctorTreatment(
            Long doctorTreatmentId, DoctorTreatmentUpdateRequest request) {
        DoctorTreatmentEntity existing = doctorTreatmentRepository.findByDoctorTreatmentId(doctorTreatmentId)
                .orElseThrow(() -> new RuntimeException("의사 진료 정보를 찾을 수 없습니다."));

        LocalDateTime start = request.getDoctorTreatmentStart() != null
                ? request.getDoctorTreatmentStart()
                : existing.getDoctorTreatmentStart();
        LocalDateTime end = request.getDoctorTreatmentEnd() != null
                ? request.getDoctorTreatmentEnd()
                : existing.getDoctorTreatmentEnd();

        if (end.isBefore(start) || end.isEqual(start)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }

        DoctorTreatmentEntity updated = DoctorTreatmentEntity.builder()
                .doctorTreatmentId(existing.getDoctorTreatmentId())
                .patientEntity(existing.getPatientEntity())
                .userEntity(existing.getUserEntity())
                .doctorTreatmentStart(start)
                .doctorTreatmentEnd(end)
                .build();

        DoctorTreatmentEntity saved = doctorTreatmentRepository.save(updated);
        return toResponse(saved);
    }

    @Transactional
    public void deleteDoctorTreatment(Long doctorTreatmentId) {
        DoctorTreatmentEntity entity = doctorTreatmentRepository.findByDoctorTreatmentId(doctorTreatmentId)
                .orElseThrow(() -> new RuntimeException("의사 진료 정보를 찾을 수 없습니다."));
        doctorTreatmentRepository.delete(entity);
    }

    public Long calculateTreatmentDuration(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }
        Duration duration = Duration.between(start, end);
        return duration.toMinutes();
    }

    public DoctorTreatmentStatisticsResponse getDoctorTreatmentStatistics(
            Long userId, LocalDateTime start, LocalDateTime end) {

        UserEntity doctor = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("의사를 찾을 수 없습니다."));

        List<DoctorTreatmentEntity> treatments = doctorTreatmentRepository
                .findByUserEntity_IdAndDoctorTreatmentStartBetween(userId, start, end);

        Long totalTreatments = (long) treatments.size();

        Long totalDuration = treatments.stream()
                .mapToLong(t -> calculateTreatmentDuration(t.getDoctorTreatmentStart(), t.getDoctorTreatmentEnd()))
                .sum();

        Double averageDuration = totalTreatments > 0
                ? (double) totalDuration / totalTreatments
                : 0.0;

        Map<LocalDate, List<DoctorTreatmentEntity>> dailyMap = treatments.stream()
                .collect(Collectors.groupingBy(t -> t.getDoctorTreatmentStart().toLocalDate()));

        List<DailyStatistics> dailyStatistics = dailyMap.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<DoctorTreatmentEntity> dayTreatments = entry.getValue();
                    Long count = (long) dayTreatments.size();
                    Long dayTotalDuration = dayTreatments.stream()
                            .mapToLong(t -> calculateTreatmentDuration(t.getDoctorTreatmentStart(), t.getDoctorTreatmentEnd()))
                            .sum();
                    return new DailyStatistics(date, count, dayTotalDuration);
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());

        return new DoctorTreatmentStatisticsResponse(
                userId,
                doctor.getName(),
                totalTreatments,
                totalDuration,
                averageDuration,
                dailyStatistics
        );
    }

    private DoctorTreatmentResponse toResponse(DoctorTreatmentEntity entity) {
        PatientEntity patient = entity.getPatientEntity();
        UserEntity doctor = entity.getUserEntity();
        Long duration = calculateTreatmentDuration(
                entity.getDoctorTreatmentStart(),
                entity.getDoctorTreatmentEnd()
        );

        return new DoctorTreatmentResponse(
                entity.getDoctorTreatmentId(),
                patient.getPatientNo(),
                patient.getPatientName(),
                doctor.getId(),
                doctor.getName(),
                entity.getDoctorTreatmentStart(),
                entity.getDoctorTreatmentEnd(),
                duration
        );
    }
}