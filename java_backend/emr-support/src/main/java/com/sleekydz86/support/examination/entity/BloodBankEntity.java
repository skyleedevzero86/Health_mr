package com.sleekydz86.support.examination.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.support.examination.entity.ExaminationEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "Blood_Bank")
@Table(name = "blood_bank")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BloodBankEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blood_bank_id", nullable = false)
    private Long bloodBankId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examination_id", referencedColumnName = "examination_id", nullable = false)
    private ExaminationEntity examinationEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_no", referencedColumnName = "patient_no", nullable = false)
    private PatientEntity patientEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", referencedColumnName = "treatment_id", nullable = false)
    private TreatmentEntity treatmentEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity userEntity;

    @Column(name = "examination_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime examinationTime;

    @Column(name = "blood_type", length = 10)
    private String bloodType;
}

