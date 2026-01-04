package com.sleekydz86.support.disability.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity(name = "Disability")
@Table(name = "disability")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DisabilityEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disability_id", nullable = false)
    private Long disabilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_no", referencedColumnName = "patient_no", nullable = false, unique = true)
    private PatientEntity patientEntity;

    @Column(name = "disability_grade", nullable = false, length = 10)
    private String disabilityGrade;

    @Column(name = "disability_type", length = 50)
    private String disabilityType;

    @Column(name = "assistive_device_YN", nullable = false, length = 1)
    private Boolean needsAssistiveDevice;

    @Column(name = "disability_device_type", length = 100)
    private String disabilityDeviceType;

    @Builder
    private DisabilityEntity(
            Long disabilityId,
            PatientEntity patientEntity,
            String disabilityGrade,
            String disabilityType,
            Boolean needsAssistiveDevice,
            String disabilityDeviceType) {
        validate(patientEntity, disabilityGrade, needsAssistiveDevice);
        this.disabilityId = disabilityId;
        this.patientEntity = patientEntity;
        this.disabilityGrade = disabilityGrade;
        this.disabilityType = disabilityType;
        this.needsAssistiveDevice = needsAssistiveDevice != null ? needsAssistiveDevice : false;
        this.disabilityDeviceType = disabilityDeviceType;
    }

    private void validate(PatientEntity patientEntity, String disabilityGrade, Boolean needsAssistiveDevice) {
        if (patientEntity == null) {
            throw new IllegalArgumentException("환자 정보는 필수입니다.");
        }
        if (disabilityGrade == null || disabilityGrade.isBlank()) {
            throw new IllegalArgumentException("장애 등급은 필수입니다.");
        }
        if (!isValidGrade(disabilityGrade)) {
            throw new IllegalArgumentException("유효하지 않은 장애 등급입니다: " + disabilityGrade);
        }
    }


    private boolean isValidGrade(String grade) {
        return grade.matches("^[1-6]급$");
    }


    public void updateInfo(String grade, String type) {
        if (grade != null && !grade.isBlank()) {
            if (!isValidGrade(grade)) {
                throw new IllegalArgumentException("유효하지 않은 장애 등급입니다: " + grade);
            }
            this.disabilityGrade = grade;
        }
        if (type != null) {
            this.disabilityType = type;
        }
    }


    public void updateAssistiveDevice(boolean needsDevice, String deviceType) {
        this.needsAssistiveDevice = needsDevice;
        if (needsDevice) {
            this.disabilityDeviceType = deviceType;
        } else {
            this.disabilityDeviceType = null;
        }
    }


    public boolean needsAssistiveDevice() {
        return Boolean.TRUE.equals(needsAssistiveDevice);
    }

    public String getDisabilityDeviceYNValue() {
        return needsAssistiveDevice != null && needsAssistiveDevice ? "Y" : "N";
    }
}
