package com.sleekydz86.support.equipment.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.equipment.entity.EquipmentEntity;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends BaseRepository<EquipmentEntity, Long> {

    Optional<EquipmentEntity> findByEquipmentId(Long equipmentId);

    List<EquipmentEntity> findAllByEquipmentName(String equipmentName);
}

