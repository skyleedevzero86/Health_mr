package com.sleekydz86.support.examination.service;

import com.sleekydz86.support.equipment.entity.EquipmentEntity;
import com.sleekydz86.support.equipment.repository.EquipmentRepository;
import com.sleekydz86.support.examination.dto.ExaminationRegisterRequest;
import com.sleekydz86.support.examination.dto.ExaminationResponse;
import com.sleekydz86.support.examination.dto.ExaminationUpdateRequest;
import com.sleekydz86.support.examination.entity.ExaminationEntity;
import com.sleekydz86.support.examination.repository.ExaminationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExaminationService {

    private final ExaminationRepository examinationRepository;
    private final EquipmentRepository equipmentRepository;

    public ExaminationService(ExaminationRepository examinationRepository, EquipmentRepository equipmentRepository) {
        this.examinationRepository = examinationRepository;
        this.equipmentRepository = equipmentRepository;
    }

    public ExaminationEntity registerExamination(ExaminationRegisterRequest request) {
        EquipmentEntity foundEquipment = null;

        if (request.getEquipmentId() != null) {
            foundEquipment = equipmentRepository.findByEquipmentId(request.getEquipmentId())
                    .orElseThrow(() -> new IllegalArgumentException("장비 정보를 찾을 수 없습니다."));
        }

        ExaminationEntity examinationEntity = ExaminationEntity.builder()
                .equipmentEntity(foundEquipment)
                .examinationName(request.getExaminationName())
                .examinationType(request.getExaminationType())
                .examinationConstraints(request.getExaminationConstraints())
                .examinationLocation(request.getExaminationLocation())
                .examinationPrice(request.getExaminationPrice())
                .build();

        return examinationRepository.save(examinationEntity);
    }

    public ExaminationResponse readExamination(Long examinationId) {
        Optional<ExaminationEntity> examinationEntityOptional = examinationRepository.findByExaminationId(examinationId);

        if (examinationEntityOptional.isPresent()) {
            ExaminationEntity examinationEntity = examinationEntityOptional.get();
            EquipmentEntity equipment = examinationEntity.getEquipmentEntity();

            return new ExaminationResponse(
                    examinationEntity.getExaminationId(),
                    equipment != null ? equipment.getEquipmentId() : null,
                    equipment != null ? equipment.getEquipmentName() : null,
                    examinationEntity.getExaminationName(),
                    examinationEntity.getExaminationType(),
                    examinationEntity.getExaminationConstraints(),
                    examinationEntity.getExaminationLocation(),
                    examinationEntity.getExaminationPrice()
            );
        }
        return null;
    }


    public List<ExaminationResponse> readExaminationByEquipmentId(Long equipmentId) {
        EquipmentEntity foundEquipment = equipmentRepository.findByEquipmentId(equipmentId)
                .orElseThrow(() -> new RuntimeException("해당 장비 정보가 없습니다."));

        List<ExaminationEntity> examinationEntities = examinationRepository.findAllByEquipmentEntity(foundEquipment);

        return examinationEntities.stream()
                .map(examinationEntity -> {
                    EquipmentEntity equipment = examinationEntity.getEquipmentEntity();
                    return new ExaminationResponse(
                            examinationEntity.getExaminationId(),
                            equipment != null ? equipment.getEquipmentId() : null,
                            equipment != null ? equipment.getEquipmentName() : null,
                            examinationEntity.getExaminationName(),
                            examinationEntity.getExaminationType(),
                            examinationEntity.getExaminationConstraints(),
                            examinationEntity.getExaminationLocation(),
                            examinationEntity.getExaminationPrice()
                    );
                })
                .collect(Collectors.toList());
    }


    public List<ExaminationResponse> readAllExamination() {
        List<ExaminationEntity> examinationEntities = examinationRepository.findAll();

        return examinationEntities.stream()
                .map(examinationEntity -> {
                    EquipmentEntity equipment = examinationEntity.getEquipmentEntity();
                    return new ExaminationResponse(
                            examinationEntity.getExaminationId(),
                            equipment != null ? equipment.getEquipmentId() : null,
                            equipment != null ? equipment.getEquipmentName() : null,
                            examinationEntity.getExaminationName(),
                            examinationEntity.getExaminationType(),
                            examinationEntity.getExaminationConstraints(),
                            examinationEntity.getExaminationLocation(),
                            examinationEntity.getExaminationPrice()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ExaminationEntity updateExamination(Long examinationId, ExaminationUpdateRequest request) {
        ExaminationEntity existingEntity = examinationRepository.findByExaminationId(examinationId)
                .orElseThrow(() -> new RuntimeException("해당 검사 정보가 없습니다."));

        EquipmentEntity foundEquipment = existingEntity.getEquipmentEntity();

        if (request.getEquipmentId() != null) {
            foundEquipment = equipmentRepository.findByEquipmentId(request.getEquipmentId())
                    .orElseThrow(() -> new RuntimeException("갱신하려는 해당 장비 정보가 없습니다."));
        }

        ExaminationEntity updatedEntity = existingEntity.toBuilder()
                .equipmentEntity(foundEquipment)
                .examinationName(request.getExaminationName() != null ? request.getExaminationName() : existingEntity.getExaminationName())
                .examinationType(request.getExaminationType() != null ? request.getExaminationType() : existingEntity.getExaminationType())
                .examinationConstraints(request.getExaminationConstraints() != null ? request.getExaminationConstraints() : existingEntity.getExaminationConstraints())
                .examinationLocation(request.getExaminationLocation() != null ? request.getExaminationLocation() : existingEntity.getExaminationLocation())
                .examinationPrice(request.getExaminationPrice() != null ? request.getExaminationPrice() : existingEntity.getExaminationPrice())
                .build();

        return examinationRepository.save(updatedEntity);
    }

    @Transactional
    public ExaminationResponse deleteExamination(Long examinationId) {
        ExaminationEntity examinationEntity = examinationRepository.findByExaminationId(examinationId)
                .orElseThrow(() -> new RuntimeException("해당 검사 정보가 없습니다."));

        EquipmentEntity equipment = examinationEntity.getEquipmentEntity();

        ExaminationResponse deletedExamination = new ExaminationResponse(
                examinationEntity.getExaminationId(),
                equipment != null ? equipment.getEquipmentId() : null,
                equipment != null ? equipment.getEquipmentName() : null,
                examinationEntity.getExaminationName(),
                examinationEntity.getExaminationType(),
                examinationEntity.getExaminationConstraints(),
                examinationEntity.getExaminationLocation(),
                examinationEntity.getExaminationPrice()
        );

        examinationRepository.delete(examinationEntity);

        return deletedExamination;
    }
}

