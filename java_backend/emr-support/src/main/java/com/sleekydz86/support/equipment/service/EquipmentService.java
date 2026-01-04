package com.sleekydz86.support.equipment.service;

import com.sleekydz86.support.equipment.dto.EquipmentRegisterRequest;
import com.sleekydz86.support.equipment.dto.EquipmentResponse;
import com.sleekydz86.support.equipment.dto.EquipmentUpdateRequest;
import com.sleekydz86.support.equipment.entity.EquipmentEntity;
import com.sleekydz86.support.equipment.repository.EquipmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public EquipmentEntity registerEquipment(EquipmentRegisterRequest request) {
        EquipmentEntity equipmentEntity = EquipmentEntity.builder()
                .equipmentName(request.getEquipmentName())
                .equipmentProductNumber(request.getEquipmentProductNumber())
                .equipmentManufacturer(request.getEquipmentManufacturer())
                .equipmentLocation(request.getEquipmentLocation())
                .equipmentState(request.getEquipmentState())
                .equipmentSchedule(request.getEquipmentSchedule())
                .build();

        return equipmentRepository.save(equipmentEntity);
    }


    public EquipmentResponse readEquipment(Long equipmentId) {
        EquipmentEntity equipmentEntity = equipmentRepository.findByEquipmentId(equipmentId)
                .orElseThrow(() -> new RuntimeException("해당 장비 정보가 없습니다."));

        return EquipmentResponse.builder()
                .equipmentId(equipmentEntity.getEquipmentId())
                .equipmentName(equipmentEntity.getEquipmentName())
                .equipmentProductNumber(equipmentEntity.getEquipmentProductNumber())
                .equipmentManufacturer(equipmentEntity.getEquipmentManufacturer())
                .equipmentLocation(equipmentEntity.getEquipmentLocation())
                .equipmentState(equipmentEntity.getEquipmentState())
                .equipmentSchedule(equipmentEntity.getEquipmentSchedule())
                .build();
    }

    public List<EquipmentResponse> readEquipmentByEquipmentName(String equipmentName) {
        List<EquipmentEntity> equipmentEntities = equipmentRepository.findAllByEquipmentName(equipmentName);

        return equipmentEntities.stream()
                .map(equipmentEntity -> EquipmentResponse.builder()
                        .equipmentId(equipmentEntity.getEquipmentId())
                        .equipmentName(equipmentEntity.getEquipmentName())
                        .equipmentProductNumber(equipmentEntity.getEquipmentProductNumber())
                        .equipmentManufacturer(equipmentEntity.getEquipmentManufacturer())
                        .equipmentLocation(equipmentEntity.getEquipmentLocation())
                        .equipmentState(equipmentEntity.getEquipmentState())
                        .equipmentSchedule(equipmentEntity.getEquipmentSchedule())
                        .build())
                .collect(Collectors.toList());
    }

    public List<EquipmentResponse> readAllEquipment() {
        List<EquipmentEntity> equipmentEntities = equipmentRepository.findAll();

        return equipmentEntities.stream()
                .map(equipmentEntity -> EquipmentResponse.builder()
                        .equipmentId(equipmentEntity.getEquipmentId())
                        .equipmentName(equipmentEntity.getEquipmentName())
                        .equipmentProductNumber(equipmentEntity.getEquipmentProductNumber())
                        .equipmentManufacturer(equipmentEntity.getEquipmentManufacturer())
                        .equipmentLocation(equipmentEntity.getEquipmentLocation())
                        .equipmentState(equipmentEntity.getEquipmentState())
                        .equipmentSchedule(equipmentEntity.getEquipmentSchedule())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public EquipmentEntity updateEquipment(Long equipmentId, EquipmentUpdateRequest request) {
        EquipmentEntity existingEntity = equipmentRepository.findByEquipmentId(equipmentId)
                .orElseThrow(() -> new RuntimeException("기존 해당 장비 정보가 없습니다."));

        boolean hasName = request.getEquipmentName() != null && !request.getEquipmentName().isBlank();
        boolean hasProductNumber = request.getEquipmentProductNumber() != null && !request.getEquipmentProductNumber().isBlank();
        boolean hasManufacturer = request.getEquipmentManufacturer() != null && !request.getEquipmentManufacturer().isBlank();

        if (hasName || hasProductNumber || hasManufacturer) {
            if (!(hasName && hasProductNumber && hasManufacturer)) {
                throw new RuntimeException("장비의 이름, 제품번호, 제조사를 모두 채우거나 모두 비워야 합니다.");
            }
        }

        EquipmentEntity updatedEntity = EquipmentEntity.builder()
                .equipmentId(equipmentId)
                .equipmentName(request.getEquipmentName() != null && !request.getEquipmentName().isBlank()
                        ? request.getEquipmentName()
                        : existingEntity.getEquipmentName())
                .equipmentProductNumber(request.getEquipmentProductNumber() != null && !request.getEquipmentProductNumber().isBlank()
                        ? request.getEquipmentProductNumber()
                        : existingEntity.getEquipmentProductNumber())
                .equipmentManufacturer(request.getEquipmentManufacturer() != null && !request.getEquipmentManufacturer().isBlank()
                        ? request.getEquipmentManufacturer()
                        : existingEntity.getEquipmentManufacturer())
                .equipmentLocation(request.getEquipmentLocation() != null && !request.getEquipmentLocation().isBlank()
                        ? request.getEquipmentLocation()
                        : existingEntity.getEquipmentLocation())
                .equipmentState(request.getEquipmentState() != null && !request.getEquipmentState().isBlank()
                        ? request.getEquipmentState()
                        : existingEntity.getEquipmentState())
                .equipmentSchedule(request.getEquipmentSchedule() != null
                        ? request.getEquipmentSchedule()
                        : existingEntity.getEquipmentSchedule())
                .build();

        return equipmentRepository.save(updatedEntity);
    }

    @Transactional
    public EquipmentResponse deleteEquipment(Long equipmentId) {
        EquipmentEntity equipmentEntity = equipmentRepository.findByEquipmentId(equipmentId)
                .orElseThrow(() -> new RuntimeException("해당 장비 정보가 없습니다."));

        EquipmentResponse deletedEquipment = EquipmentResponse.builder()
                .equipmentId(equipmentEntity.getEquipmentId())
                .equipmentName(equipmentEntity.getEquipmentName())
                .equipmentProductNumber(equipmentEntity.getEquipmentProductNumber())
                .equipmentManufacturer(equipmentEntity.getEquipmentManufacturer())
                .equipmentLocation(equipmentEntity.getEquipmentLocation())
                .equipmentState(equipmentEntity.getEquipmentState())
                .equipmentSchedule(equipmentEntity.getEquipmentSchedule())
                .build();

        equipmentRepository.delete(equipmentEntity);

        return deletedEquipment;
    }
}