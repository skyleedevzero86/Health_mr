package com.sleekydz86.support.doctortreatment.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.doctortreatment.entity.DoctorTreatmentEntity;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorTreatmentRepository extends BaseRepository<DoctorTreatmentEntity, Long> {

    Optional<DoctorTreatmentEntity> findByDoctorTreatmentId(Long doctorTreatmentId);

    List<DoctorTreatmentEntity> findByUserEntity_Id(Long userId);

    List<DoctorTreatmentEntity> findByPatientEntity_PatientNo(Long patientNo);

    List<DoctorTreatmentEntity> findByUserEntity_IdAndDoctorTreatmentStartBetween(
            Long userId, LocalDateTime start, LocalDateTime end);

    Long countByUserEntity_Id(Long userId);
}

