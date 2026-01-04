package com.sleekydz86.support.examination.journal.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.examination.entity.ExaminationScheduleEntity;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExaminationScheduleRepository extends BaseRepository<ExaminationScheduleEntity, Long> {

    List<ExaminationScheduleEntity> findByPatientEntity_PatientNo(Long patientNo);

    List<ExaminationScheduleEntity> findByExaminationEntity_ExaminationId(Long examinationId);

    List<ExaminationScheduleEntity> findByTreatmentEntity_TreatmentId(Long treatmentId);

    List<ExaminationScheduleEntity> findByUserEntity_Id(Long userId);

    List<ExaminationScheduleEntity> findByExaminationDate(LocalDate date);

    List<ExaminationScheduleEntity> findByExaminationDateBetween(LocalDate start, LocalDate end);
}

