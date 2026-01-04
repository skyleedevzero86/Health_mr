package com.sleekydz86.support.examination.journal.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.examination.entity.BloodBankEntity;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BloodBankRepository extends BaseRepository<BloodBankEntity, Long> {

    List<BloodBankEntity> findByPatientEntity_PatientNo(Long patientNo);

    List<BloodBankEntity> findByExaminationEntity_ExaminationId(Long examinationId);

    List<BloodBankEntity> findByTreatmentEntity_TreatmentId(Long treatmentId);

    List<BloodBankEntity> findByUserEntity_Id(Long userId);

    List<BloodBankEntity> findByBloodType(String bloodType);

    List<BloodBankEntity> findByExaminationTimeBetween(LocalDateTime start, LocalDateTime end);
}

