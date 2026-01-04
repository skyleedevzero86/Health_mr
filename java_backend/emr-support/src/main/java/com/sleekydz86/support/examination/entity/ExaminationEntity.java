package com.sleekydz86.support.examination.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.support.equipment.entity.EquipmentEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity(name = "Examination")
@Table(name = "examination")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExaminationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "examination_id", nullable = false)
    private Long examinationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", referencedColumnName = "equipment_id")
    private EquipmentEntity equipmentEntity;

    @Column(name = "examination_name", nullable = false, length = 100)
    private String examinationName;

    @Column(name = "examination_type", nullable = false, length = 50)
    private String examinationType;

    @Column(name = "examination_constraints", columnDefinition = "TEXT")
    private String examinationConstraints;

    @Column(name = "examination_location", nullable = false, length = 100)
    private String examinationLocation;

    @Column(name = "examination_price", nullable = false)
    @Min(value = 0, message = "검사 가격은 0 이상이어야 합니다.")
    private Long examinationPrice;

    @Builder
    private ExaminationEntity(
            Long examinationId,
            EquipmentEntity equipmentEntity,
            String examinationName,
            String examinationType,
            String examinationConstraints,
            String examinationLocation,
            Long examinationPrice) {
        validate(examinationName, examinationType, examinationLocation, examinationPrice);
        this.examinationId = examinationId;
        this.equipmentEntity = equipmentEntity;
        this.examinationName = examinationName;
        this.examinationType = examinationType;
        this.examinationConstraints = examinationConstraints;
        this.examinationLocation = examinationLocation;
        this.examinationPrice = examinationPrice;
    }

    private void validate(String name, String type, String location, Long price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("검사명은 필수입니다.");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("검사 유형은 필수입니다.");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("검사 위치는 필수입니다.");
        }
        if (price == null || price < 0) {
            throw new IllegalArgumentException("검사 가격은 0 이상이어야 합니다.");
        }
    }

    public void updateInfo(String name, String type, String location) {
        if (name != null && !name.isBlank()) {
            this.examinationName = name;
        }
        if (type != null && !type.isBlank()) {
            this.examinationType = type;
        }
        if (location != null && !location.isBlank()) {
            this.examinationLocation = location;
        }
    }

    public void updateConstraints(String constraints) {
        this.examinationConstraints = constraints;
    }

    public void updatePrice(Long newPrice) {
        if (newPrice == null || newPrice < 0) {
            throw new IllegalArgumentException("검사 가격은 0 이상이어야 합니다.");
        }
        this.examinationPrice = newPrice;
    }

    public void assignEquipment(EquipmentEntity equipment) {
        this.equipmentEntity = equipment;
    }

    public void unassignEquipment() {
        this.equipmentEntity = null;
    }

    public boolean hasEquipment() {
        return this.equipmentEntity != null;
    }

    public boolean hasPrice() {
        return this.examinationPrice != null && this.examinationPrice > 0;
    }
}