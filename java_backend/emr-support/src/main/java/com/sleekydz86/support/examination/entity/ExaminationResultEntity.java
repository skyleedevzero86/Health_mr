package com.sleekydz86.support.examination.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity(name = "Examination_Result")
@Table(name = "examination_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExaminationResultEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "examination_result_id", nullable = false)
    private Long examinationResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examination_id", referencedColumnName = "examination_id", nullable = false)
    private ExaminationEntity examinationEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_no", referencedColumnName = "patient_no", nullable = false)
    private PatientEntity patientEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", referencedColumnName = "treatment_id", nullable = false)
    private TreatmentEntity treatmentEntity;

    @Column(name = "examination_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate examinationDate;

    @Column(name = "examination_result", columnDefinition = "TEXT")
    private String examinationResult;

    @Column(name = "examination_normal")
    private Boolean examinationNormal;

    @Column(name = "examination_notes", columnDefinition = "TEXT")
    private String examinationNotes;
}

