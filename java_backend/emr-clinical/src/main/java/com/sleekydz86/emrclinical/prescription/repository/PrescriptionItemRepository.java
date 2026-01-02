package com.sleekydz86.emrclinical.prescription.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.emrclinical.prescription.entity.PrescriptionItemEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionItemRepository extends BaseRepository<PrescriptionItemEntity, Long> {

    List<PrescriptionItemEntity> findByPrescriptionEntity_PrescriptionId(Long prescriptionId);
}