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
public interface TreatmentRepository extends BaseRepository<TreatmentEntity, Long>, TreatmentRepositoryCustom {

    @Query("SELECT t FROM Treatments t WHERE t.patientEntity.patientNo.value = :patientNo")
    List<TreatmentEntity> findByPatientEntity_PatientNo(@Param("patientNo") Long patientNo);

    @Query("SELECT t FROM Treatments t WHERE t.patientEntity.patientNo.value = :patientNo")
    Page<TreatmentEntity> findByPatientEntity_PatientNo(@Param("patientNo") Long patientNo, Pageable pageable);

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

    @Query("SELECT t FROM Treatments t LEFT JOIN FETCH t.checkInEntity ci LEFT JOIN FETCH ci.patientEntity LEFT JOIN FETCH t.patientEntity LEFT JOIN FETCH t.treatmentDoc LEFT JOIN FETCH t.departmentEntity WHERE t.treatmentId = :treatmentId")
    java.util.Optional<TreatmentEntity> findDetailById(@Param("treatmentId") Long treatmentId);

    @Query(value = "select patient_no from treatments where treatment_id = :treatmentId", nativeQuery = true)
    Long findStoredPatientNo(@Param("treatmentId") Long treatmentId);



}

