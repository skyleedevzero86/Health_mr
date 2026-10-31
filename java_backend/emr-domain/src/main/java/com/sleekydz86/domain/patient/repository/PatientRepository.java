package com.sleekydz86.domain.patient.repository;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface PatientRepository extends JpaRepository<PatientEntity, Long>, PatientRepositoryCustom {

    @Query("SELECT p FROM Patient p WHERE p.patientNo.value = :patientNo")
    Optional<PatientEntity> findByPatientNo(@Param("patientNo") Long patientNo);

    List<PatientEntity> findByPatientName(String patientName);

    List<PatientEntity> findByPatientNameContaining(String name);

    Page<PatientEntity> findByPatientNameContaining(String name, Pageable pageable);

    List<PatientEntity> findByRrnHmac(String rrnHmac);

    boolean existsByRrnHmac(String rrnHmac);

    List<PatientEntity> findByEmailHmac(String emailHmac);

    boolean existsByEmailHmac(String emailHmac);

    List<PatientEntity> findByTelHmac(String telHmac);

    boolean existsByTelHmac(String telHmac);

    List<PatientEntity> findByPassportHmac(String passportHmac);

    boolean existsByPassportHmac(String passportHmac);

    List<PatientEntity> findAllByOrderByPatientLastVisitDesc();

    @Query("SELECT p FROM Patient p WHERE p.patientLastVisit >= :since")
    List<PatientEntity> findRecentPatients(@Param("since") LocalDate since);

    @Query("SELECT p FROM Patient p ORDER BY p.patientNo.value DESC")
    List<PatientEntity> findLatestByPatientNo(Pageable pageable);
}

