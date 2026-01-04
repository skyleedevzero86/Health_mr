package com.sleekydz86.support.examination.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.equipment.entity.EquipmentEntity;
import com.sleekydz86.support.examination.entity.ExaminationEntity;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExaminationRepository extends BaseRepository<ExaminationEntity, Long> {

    Optional<ExaminationEntity> findByExaminationId(Long examinationId);

    ExaminationEntity findByExaminationName(String examinationName);

    List<ExaminationEntity> findAllByEquipmentEntity(EquipmentEntity equipment);
}

