package com.sleekydz86.support.equipment.journal.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.equipment.entity.EquipmentJournalEntity;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EquipmentJournalRepository extends BaseRepository<EquipmentJournalEntity, Long> {

    List<EquipmentJournalEntity> findByEquipmentEntity_EquipmentId(Long equipmentId);

    List<EquipmentJournalEntity> findByUserEntity_Id(Long userId);

    List<EquipmentJournalEntity> findByEquipmentInspectionDate(LocalDate inspectionDate);

    List<EquipmentJournalEntity> findByEquipmentEntity_EquipmentIdAndEquipmentInspectionDateBetween(
            Long equipmentId, LocalDate start, LocalDate end);
}

