package com.sleekydz86.finance.medicalfee.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "MedicalProcedureCodeStatistics")
@Table(name = "medical_procedure_code_statistics", indexes = {
    @Index(name = "idx_procedure_code", columnList = "procedure_code"),
    @Index(name = "idx_treatment_year", columnList = "treatment_year"),
    @Index(name = "idx_institution_type", columnList = "institution_type"),
    @Index(name = "idx_procedure_code_year", columnList = "procedure_code, treatment_year"),
    @Index(name = "uk_procedure_year_institution", columnList = "procedure_code, treatment_year, institution_type", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicalProcedureCodeStatisticsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statistics_id", nullable = false)
    private Long statisticsId;

    @Column(name = "procedure_code", nullable = false, length = 20)
    @NotBlank
    private String procedureCode;

    @Column(name = "treatment_year", nullable = false, length = 4)
    @NotBlank
    private String treatmentYear;

    @Column(name = "institution_type", nullable = false, length = 50)
    @NotBlank
    private String institutionType;

    @Column(name = "patient_count", nullable = false)
    private Long patientCount;

    @Column(name = "treatment_count", nullable = false)
    private Long treatmentCount;

    @Column(name = "data_source", length = 100)
    private String dataSource;

    @Column(name = "data_date")
    private LocalDate dataDate;

    @Builder
    private MedicalProcedureCodeStatisticsEntity(
            Long statisticsId,
            String procedureCode,
            String treatmentYear,
            String institutionType,
            Long patientCount,
            Long treatmentCount,
            String dataSource,
            LocalDate dataDate) {
        this.statisticsId = statisticsId;
        this.procedureCode = procedureCode;
        this.treatmentYear = treatmentYear;
        this.institutionType = institutionType;
        this.patientCount = patientCount != null ? patientCount : 0L;
        this.treatmentCount = treatmentCount != null ? treatmentCount : 0L;
        this.dataSource = dataSource;
        this.dataDate = dataDate;
    }
}

