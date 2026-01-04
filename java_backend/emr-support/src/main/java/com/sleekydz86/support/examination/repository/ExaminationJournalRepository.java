package com.sleekydz86.support.examination.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.support.examination.entity.ExaminationJournalEntity;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExaminationJournalRepository extends BaseRepository<ExaminationJournalEntity, Long> {

    List<ExaminationJournalEntity> findExaminationJournalEntitiesByPatientEntity(PatientEntity patientEntity);

    List<ExaminationJournalEntity> findByPatientEntity_PatientNo(Long patientNo);

    List<ExaminationJournalEntity> findByExaminationEntity_ExaminationId(Long examinationId);

    List<ExaminationJournalEntity> findByTreatmentEntity_TreatmentId(Long treatmentId);

    List<ExaminationJournalEntity> findByUserEntity_Id(Long userId);

    List<ExaminationJournalEntity> findByEquipmentEntity_EquipmentId(Long equipmentId);

    List<ExaminationJournalEntity> findByExaminationTimeBetween(LocalDateTime start, LocalDateTime end);
}

