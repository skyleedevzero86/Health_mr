package com.sleekydz86.support.equipment.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity(name = "Equipment")
@Table(name = "equipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquipmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "equipment_name", nullable = false, length = 100)
    private String equipmentName;

    @Column(name = "equipment_product_number", nullable = false, length = 100)
    private String equipmentProductNumber;

    @Column(name = "equipment_manufacturer", nullable = false, length = 100)
    private String equipmentManufacturer;

    @Column(name = "equipment_location", nullable = false, length = 100)
    private String equipmentLocation;

    @Column(name = "equipment_state", nullable = false, length = 20)
    private String equipmentState;

    @Column(name = "equipment_schedule")
    private LocalDate equipmentSchedule;

    @Builder
    private EquipmentEntity(
            Long equipmentId,
            String equipmentName,
            String equipmentProductNumber,
            String equipmentManufacturer,
            String equipmentLocation,
            String equipmentState,
            LocalDate equipmentSchedule) {
        validate(equipmentName, equipmentProductNumber, equipmentManufacturer, equipmentLocation, equipmentState);
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.equipmentProductNumber = equipmentProductNumber;
        this.equipmentManufacturer = equipmentManufacturer;
        this.equipmentLocation = equipmentLocation;
        this.equipmentState = equipmentState;
        this.equipmentSchedule = equipmentSchedule;
    }

    private void validate(String name, String productNumber, String manufacturer,
                          String location, String state) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("장비명은 필수입니다.");
        }
        if (productNumber == null || productNumber.isBlank()) {
            throw new IllegalArgumentException("제품 번호는 필수입니다.");
        }
        if (manufacturer == null || manufacturer.isBlank()) {
            throw new IllegalArgumentException("제조사는 필수입니다.");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("장비 위치는 필수입니다.");
        }
        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("장비 상태는 필수입니다.");
        }
        if (!isValidState(state)) {
            throw new IllegalArgumentException("유효하지 않은 장비 상태입니다: " + state);
        }
    }

    private boolean isValidState(String state) {
        return state.equals("AVAILABLE") || state.equals("IN_USE") ||
                state.equals("MAINTENANCE") || state.equals("BROKEN") ||
                state.equals("RETIRED");
    }

    public void updateInfo(String name, String productNumber, String manufacturer) {
        if (name == null && productNumber == null && manufacturer == null) {
            return;
        }

        boolean allNull = name == null && productNumber == null && manufacturer == null;
        boolean allNotNull = name != null && productNumber != null && manufacturer != null;

        if (!allNull && !allNotNull) {
            throw new IllegalArgumentException("장비명, 제품번호, 제조사는 모두 함께 업데이트되어야 합니다.");
        }

        if (allNotNull) {
            this.equipmentName = name;
            this.equipmentProductNumber = productNumber;
            this.equipmentManufacturer = manufacturer;
        }
    }

    public void updateLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("장비 위치는 필수입니다.");
        }
        this.equipmentLocation = location;
    }

    public void changeState(String newState) {
        if (newState == null || newState.isBlank()) {
            throw new IllegalArgumentException("장비 상태는 필수입니다.");
        }
        if (!isValidState(newState)) {
            throw new IllegalArgumentException("유효하지 않은 장비 상태입니다: " + newState);
        }
        this.equipmentState = newState;
    }

    public void setAvailable() {
        this.equipmentState = "AVAILABLE";
    }

    public void setInUse() {
        this.equipmentState = "IN_USE";
    }

    public void setMaintenance() {
        this.equipmentState = "MAINTENANCE";
    }

    public void setBroken() {
        this.equipmentState = "BROKEN";
    }

    public void setRetired() {
        this.equipmentState = "RETIRED";
    }

    public void updateSchedule(LocalDate schedule) {
        this.equipmentSchedule = schedule;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equals(this.equipmentState);
    }

    public boolean isInUse() {
        return "IN_USE".equals(this.equipmentState);
    }

    public boolean isInMaintenance() {
        return "MAINTENANCE".equals(this.equipmentState);
    }
}
