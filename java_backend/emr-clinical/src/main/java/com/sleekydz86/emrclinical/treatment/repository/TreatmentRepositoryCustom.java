package com.sleekydz86.emrclinical.treatment.repository;

import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.types.TreatmentStatus;
import com.sleekydz86.emrclinical.types.TreatmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface TreatmentRepositoryCustom {
    List<TreatmentEntity> findByConditions(
            Long patientNo,
            Long doctorId,
            Long departmentId,
            TreatmentType treatmentType,
            TreatmentStatus treatmentStatus,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    Page<TreatmentEntity> findByConditionsWithPaging(
            Long patientNo,
            Long doctorId,
            Long departmentId,
            TreatmentType treatmentType,
            TreatmentStatus treatmentStatus,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
    List<TreatmentEntity> findByCheckInIds(List<Long> checkInIds);
    Long countByDoctorAndDateRange(Long doctorId, LocalDateTime start, LocalDateTime end);
    List<TreatmentEntity> findTodayTreatments(LocalDate date);
    Long countByDateRange(LocalDateTime start, LocalDateTime end);
}
