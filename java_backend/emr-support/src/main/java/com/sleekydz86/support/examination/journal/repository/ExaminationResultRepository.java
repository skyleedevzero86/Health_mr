package com.sleekydz86.support.examination.journal.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.examination.entity.ExaminationResultEntity;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExaminationResultRepository extends BaseRepository<ExaminationResultEntity, Long> {

    List<ExaminationResultEntity> findByPatientEntity_PatientNo(Long patientNo);

    List<ExaminationResultEntity> findByExaminationEntity_ExaminationId(Long examinationId);

    List<ExaminationResultEntity> findByTreatmentEntity_TreatmentId(Long treatmentId);

    List<ExaminationResultEntity> findByExaminationDate(LocalDate date);

    List<ExaminationResultEntity> findByExaminationDateBetween(LocalDate start, LocalDate end);

    List<ExaminationResultEntity> findByExaminationNormal(Boolean normal);
}

