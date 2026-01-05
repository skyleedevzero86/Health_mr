package com.sleekydz86.support.disability.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "DisabilityCareInstitution")
@Table(name = "disability_care_institution", indexes = {
        @Index(name = "idx_service_type", columnList = "service_type"),
        @Index(name = "idx_sido", columnList = "sido"),
        @Index(name = "idx_institution_type", columnList = "institution_type"),
        @Index(name = "idx_is_active", columnList = "is_active")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DisabilityCareInstitutionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "institution_type", nullable = false, length = 20)
    @NotBlank
    private String institutionType;

    @Column(name = "institution_name", nullable = false, length = 200)
    @NotBlank
    private String institutionName;

    @Column(name = "service_type", nullable = false, length = 50)
    @NotBlank
    private String serviceType;

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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    private DisabilityCareInstitutionEntity(
            Long institutionId,
            String institutionType,
            String institutionName,
            String serviceType,
            String address,
            String sido,
            String sigungu,
            Double latitude,
            Double longitude,
            Boolean isActive) {
        this.institutionId = institutionId;
        this.institutionType = institutionType;
        this.institutionName = institutionName;
        this.serviceType = serviceType;
        this.address = address;
        this.sido = sido;
        this.sigungu = sigungu;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isActive = isActive != null ? isActive : true;
    }
}
