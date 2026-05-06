package com.sleekydz86.emrclinical.treatment.statistics.department.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "TreatmentDepartmentStatistics")
@Table(name = "treatment_department_statistics", indexes = {
        @Index(name = "idx_statistics_year", columnList = "statistics_year"),
        @Index(name = "idx_department_name", columnList = "department_name"),
        @Index(name = "idx_region_code", columnList = "region_code"),
        @Index(name = "idx_year_department", columnList = "statistics_year, department_name"),
        @Index(name = "idx_year_region", columnList = "statistics_year, region_code"),
        @Index(name = "uk_year_region_department", columnList = "statistics_year, region_code, department_name", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TreatmentDepartmentStatisticsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statistics_id", nullable = false)
    private Long statisticsId;

    @Column(name = "statistics_year", nullable = false, length = 4)
    @NotBlank
    private String statisticsYear;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @Column(name = "region_name", length = 50)
    private String regionName;

    @Column(name = "department_name", nullable = false, length = 100)
    @NotBlank
    private String departmentName;

    @Column(name = "patient_count", nullable = false)
    private Long patientCount;

    @Column(name = "treatment_count", nullable = false)
    private Long treatmentCount;

    @Column(name = "medical_fee", nullable = false)
    private Long medicalFee;

    @Column(name = "benefit_fee", nullable = false)
    private Long benefitFee;

    @Column(name = "data_source", length = 100)
    private String dataSource;

    @Column(name = "data_date")
    private LocalDate dataDate;

    @Builder
    private TreatmentDepartmentStatisticsEntity(
            Long statisticsId,
            String statisticsYear,
            String regionCode,
            String regionName,
            String departmentName,
            Long patientCount,
            Long treatmentCount,
            Long medicalFee,
            Long benefitFee,
            String dataSource,
            LocalDate dataDate) {
        this.statisticsId = statisticsId;
        this.statisticsYear = statisticsYear;
        this.regionCode = regionCode;
        this.regionName = regionName;
        this.departmentName = departmentName;
        this.patientCount = patientCount != null ? patientCount : 0L;
        this.treatmentCount = treatmentCount != null ? treatmentCount : 0L;
        this.medicalFee = medicalFee != null ? medicalFee : 0L;
        this.benefitFee = benefitFee != null ? benefitFee : 0L;
        this.dataSource = dataSource;
        this.dataDate = dataDate;
    }
}
