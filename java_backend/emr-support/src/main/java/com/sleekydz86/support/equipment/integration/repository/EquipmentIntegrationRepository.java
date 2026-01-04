package com.sleekydz86.support.equipment.integration.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.equipment.integration.entity.EquipmentIntegrationEntity;
import com.sleekydz86.support.equipment.integration.type.IntegrationProtocol;
import com.sleekydz86.support.equipment.integration.type.IntegrationStatus;
import com.sleekydz86.support.equipment.integration.type.IntegrationType;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentIntegrationRepository extends BaseRepository<EquipmentIntegrationEntity, Long> {

    Optional<EquipmentIntegrationEntity> findByEquipmentEntity_EquipmentId(Long equipmentId);

    List<EquipmentIntegrationEntity> findByIntegrationType(IntegrationType type);

    List<EquipmentIntegrationEntity> findByIntegrationProtocol(IntegrationProtocol protocol);

    List<EquipmentIntegrationEntity> findByIntegrationStatus(IntegrationStatus status);
}

