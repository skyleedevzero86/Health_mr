package com.sleekydz86.emrclinical.treatment.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.types.TreatmentStatus;
import com.sleekydz86.emrclinical.types.TreatmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TreatmentRepository extends BaseRepository<TreatmentEntity, Long> {

    List<TreatmentEntity> findByPatientEntity_PatientNo(Long patientNo);

    @Query("SELECT t FROM Treatments t WHERE t.checkInEntity.patientEntity.patientNo = :patientNo")
    Page<TreatmentEntity> findByPatientNo(@Param("patientNo") Long patientNo, Pageable pageable);

    List<TreatmentEntity> findByTreatmentDoc_Id(Long doctorId);

    Page<TreatmentEntity> findByTreatmentDoc_Id(Long doctorId, Pageable pageable);

    List<TreatmentEntity> findByTreatmentType(TreatmentType type);

    Page<TreatmentEntity> findByTreatmentType(TreatmentType type, Pageable pageable);

    List<TreatmentEntity> findByTreatmentStatus(TreatmentStatus status);

    Page<TreatmentEntity> findByTreatmentStatus(TreatmentStatus status, Pageable pageable);

    List<TreatmentEntity> findByCheckInEntity_CheckInId(Long checkInId);

    List<TreatmentEntity> findByTreatmentDateBetween(LocalDateTime start, LocalDateTime end);

    List<TreatmentEntity> findByDepartmentEntity_Id(Long departmentId);

    Page<TreatmentEntity> findAll(Pageable pageable);

    List<TreatmentEntity> findAllByOrderByTreatmentDateDesc();

    @Query("SELECT t FROM Treatments t WHERE DATE(t.treatmentDate) = :date")
    List<TreatmentEntity> findTodayTreatments(@Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM Treatments t WHERE t.treatmentDate BETWEEN :start AND :end")
    Long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Treatments t WHERE t.treatmentDoc.id = :doctorId AND t.treatmentDate BETWEEN :start AND :end")
    Long countByDoctorAndDateRange(@Param("doctorId") Long doctorId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

