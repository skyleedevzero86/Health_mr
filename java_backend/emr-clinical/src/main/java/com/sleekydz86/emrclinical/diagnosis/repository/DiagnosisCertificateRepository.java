package com.sleekydz86.emrclinical.diagnosis.repository;

import com.sleekydz86.emrclinical.diagnosis.entity.DiagnosisCertificateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiagnosisCertificateRepository extends JpaRepository<DiagnosisCertificateEntity, Long> {

    Optional<DiagnosisCertificateEntity> findByCertificateNumber(String certificateNumber);

    Page<DiagnosisCertificateEntity> findByPatient_Id(Long patientId, Pageable pageable);

    Page<DiagnosisCertificateEntity> findByDoctor_Id(Long doctorId, Pageable pageable);

    List<DiagnosisCertificateEntity> findByDiagnosisDateBetween(LocalDate start, LocalDate end);

    long countByDiagnosisDateBetween(LocalDate start, LocalDate end);
}
