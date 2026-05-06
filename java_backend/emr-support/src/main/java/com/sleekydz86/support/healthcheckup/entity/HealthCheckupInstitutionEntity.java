package com.sleekydz86.support.healthcheckup.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "HealthCheckupInstitution")
@Table(name = "health_checkup_institution", indexes = {
    @Index(name = "idx_region_code", columnList = "region_code"),
    @Index(name = "idx_institution_type", columnList = "institution_type"),
    @Index(name = "idx_institution_name", columnList = "institution_name"),
    @Index(name = "idx_is_active", columnList = "is_active"),
    @Index(name = "idx_region_type", columnList = "region_code, institution_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthCheckupInstitutionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @Column(name = "region_name", length = 50)
    private String regionName;

    @Column(name = "institution_name", nullable = false, length = 200)
    @NotBlank
    private String institutionName;

    @Column(name = "institution_type", nullable = false, length = 50)
    @NotBlank
    private String institutionType;

    @Column(name = "address", nullable = false, length = 500)
    @NotBlank
    private String address;

    @Column(name = "sido", length = 50)
    private String sido;

    @Column(name = "sigungu", length = 50)
    private String sigungu;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "data_source", length = 100)
    private String dataSource;

    @Column(name = "data_date")
    private LocalDate dataDate;

    @Builder
    private HealthCheckupInstitutionEntity(
            Long institutionId,
            String regionCode,
            String regionName,
            String institutionName,
            String institutionType,
            String address,
            String sido,
            String sigungu,
            Double latitude,
            Double longitude,
            String phoneNumber,
            Boolean isActive,
            String dataSource,
            LocalDate dataDate) {
        this.institutionId = institutionId;
        this.regionCode = regionCode;
        this.regionName = regionName;
        this.institutionName = institutionName;
        this.institutionType = institutionType;
        this.address = address;
        this.sido = sido;
        this.sigungu = sigungu;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phoneNumber = phoneNumber;
        this.isActive = isActive != null ? isActive : true;
        this.dataSource = dataSource;
        this.dataDate = dataDate;
    }

    public void update(
            String institutionName,
            String institutionType,
            String address,
            String phoneNumber) {
        if (institutionName != null && !institutionName.isBlank()) {
            this.institutionName = institutionName;
        }
        if (institutionType != null && !institutionType.isBlank()) {
            this.institutionType = institutionType;
        }
        if (address != null && !address.isBlank()) {
            this.address = address;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }
}

