package com.sleekydz86.support.disability.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.support.disability.entity.DisabilityEntity;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DisabilityRepository extends BaseRepository<DisabilityEntity, Long> {
    Optional<DisabilityEntity> findByPatientEntity_PatientNo(Long patientNo);
    DisabilityEntity findByPatientEntity(PatientEntity patient);
}

