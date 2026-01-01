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


public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

    @Query("SELECT p FROM Patient p WHERE p.patientNo.value = :patientNo")
    Optional<PatientEntity> findByPatientNo(@Param("patientNo") Long patientNo);

    List<PatientEntity> findByPatientName(String patientName);

    List<PatientEntity> findByPatientNameContaining(String name);

    Page<PatientEntity> findByPatientNameContaining(String name, Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE p.patientRrn.value = :patientRrn")
    Optional<PatientEntity> findByPatientRrn(@Param("patientRrn") String patientRrn);

    @Query("SELECT COUNT(p) > 0 FROM Patient p WHERE p.patientRrn.value = :patientRrn")
    boolean existsByPatientRrn(@Param("patientRrn") String patientRrn);

    @Query("SELECT p FROM Patient p WHERE p.patientEmail.value = :email")
    Optional<PatientEntity> findByPatientEmail(@Param("email") String email);

    @Query("SELECT COUNT(p) > 0 FROM Patient p WHERE p.patientEmail.value = :email")
    boolean existsByPatientEmail(@Param("email") String email);

    @Query("SELECT p FROM Patient p WHERE p.patientTel.value = :tel")
    Optional<PatientEntity> findByPatientTel(@Param("tel") String tel);

    @Query("SELECT COUNT(p) > 0 FROM Patient p WHERE p.patientTel.value = :tel")
    boolean existsByPatientTel(@Param("tel") String tel);

    List<PatientEntity> findAllByOrderByPatientLastVisitDesc();

    @Query("SELECT p FROM Patient p WHERE " +
            "(:name IS NULL OR p.patientName LIKE %:name%) AND " +
            "(:tel IS NULL OR p.patientTel.value LIKE %:tel%) AND " +
            "(:email IS NULL OR p.patientEmail.value LIKE %:email%)")
    List<PatientEntity> searchPatients(
            @Param("name") String name,
            @Param("tel") String tel,
            @Param("email") String email
    );

    @Query("SELECT p FROM Patient p WHERE p.patientLastVisit >= :since")
    List<PatientEntity> findRecentPatients(@Param("since") LocalDate since);

    @Query("SELECT p FROM Patient p ORDER BY p.patientNo.value DESC")
    Optional<PatientEntity> findTopByOrderByPatientNoDesc();
}

