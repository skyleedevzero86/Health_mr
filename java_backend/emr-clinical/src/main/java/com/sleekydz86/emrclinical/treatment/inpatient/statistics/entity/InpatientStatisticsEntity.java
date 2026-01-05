package com.sleekydz86.emrclinical.treatment.inpatient.statistics.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "InpatientStatistics")
@Table(name = "inpatient_statistics", indexes = {
    @Index(name = "idx_statistics_year", columnList = "statistics_year"),
    @Index(name = "idx_institution_type", columnList = "institution_type"),
    @Index(name = "idx_region_code", columnList = "region_code"),
    @Index(name = "idx_year_region", columnList = "statistics_year, region_code")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InpatientStatisticsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statistics_id", nullable = false)
    private Long statisticsId;

    @Column(name = "statistics_year", nullable = false, length = 4)
    @NotBlank
    private String statisticsYear;

    @Column(name = "institution_type", nullable = false, length = 50)
    @NotBlank
    private String institutionType;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @Column(name = "region_name", length = 50)
    private String regionName;

    @Column(name = "visit_days", nullable = false)
    private Long visitDays;

    @Column(name = "benefit_days", nullable = false)
    private Long benefitDays;

    @Column(name = "medical_fee", nullable = false)
    private Long medicalFee;

    @Column(name = "benefit_fee", nullable = false)
    private Long benefitFee;

    @Column(name = "data_source", length = 100)
    private String dataSource;

    @Column(name = "data_date")
    private LocalDate dataDate;

    @Builder
    private InpatientStatisticsEntity(
            Long statisticsId,
            String statisticsYear,
            String institutionType,
            String regionCode,
            String regionName,
            Long visitDays,
            Long benefitDays,
            Long medicalFee,
            Long benefitFee,
            String dataSource,
            LocalDate dataDate) {
        this.statisticsId = statisticsId;
        this.statisticsYear = statisticsYear;
        this.institutionType = institutionType;
        this.regionCode = regionCode;
        this.regionName = regionName;
        this.visitDays = visitDays != null ? visitDays : 0L;
        this.benefitDays = benefitDays != null ? benefitDays : 0L;
        this.medicalFee = medicalFee != null ? medicalFee : 0L;
        this.benefitFee = benefitFee != null ? benefitFee : 0L;
        this.dataSource = dataSource;
        this.dataDate = dataDate;
    }
}

