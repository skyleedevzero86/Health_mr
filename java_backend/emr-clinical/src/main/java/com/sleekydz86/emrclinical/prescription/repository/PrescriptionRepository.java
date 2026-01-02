package com.sleekydz86.emrclinical.prescription.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.emrclinical.prescription.entity.PrescriptionEntity;
import com.sleekydz86.emrclinical.types.PrescriptionStatus;
import com.sleekydz86.emrclinical.types.PrescriptionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends BaseRepository<PrescriptionEntity, Long> {

    Optional<PrescriptionEntity> findByTreatmentEntity_TreatmentId(Long treatmentId);

    List<PrescriptionEntity> findByPatientEntity_PatientNo(Long patientNo);

    Page<PrescriptionEntity> findByPatientEntity_PatientNo(Long patientNo, Pageable pageable);

    List<PrescriptionEntity> findByPrescriptionDoc_Id(Long doctorId);

    Page<PrescriptionEntity> findByPrescriptionDoc_Id(Long doctorId, Pageable pageable);

    List<PrescriptionEntity> findByPrescriptionStatus(PrescriptionStatus status);

    Page<PrescriptionEntity> findByPrescriptionStatus(PrescriptionStatus status, Pageable pageable);

    List<PrescriptionEntity> findByPrescriptionType(PrescriptionType type);

    Page<PrescriptionEntity> findByPrescriptionType(PrescriptionType type, Pageable pageable);

    List<PrescriptionEntity> findByPrescriptionDateBetween(LocalDateTime start, LocalDateTime end);

    Page<PrescriptionEntity> findAll(Pageable pageable);

    List<PrescriptionEntity> findAllByOrderByPrescriptionDateDesc();

    @Query("SELECT p FROM Prescription p WHERE DATE(p.prescriptionDate) = :date")
    List<PrescriptionEntity> findTodayPrescriptions(@Param("date") LocalDate date);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.prescriptionDate BETWEEN :start AND :end")
    Long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

