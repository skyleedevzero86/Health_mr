package com.sleekydz86.emrclinical.diagnosis.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 진단서 발급 엔티티.
 * 환자의 진단 내용과 발급 이력을 관리합니다.
 */
@Entity
@Table(name = "diagnosis_certificate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DiagnosisCertificateEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certificateId;

    @Column(nullable = false, unique = true, length = 20)
    private String certificateNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private UserEntity doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kcd_code_id", nullable = false)
    private KcdCodeEntity kcdCode;

    @Column(nullable = false, length = 200)
    private String diagnosisName;

    @Column(nullable = false)
    private LocalDate diagnosisDate;

    @Column(length = 1000)
    private String clinicalFindings;

    @Column(length = 500)
    private String purpose;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CertificateStatus status = CertificateStatus.ISSUED;

    public enum CertificateStatus {
        ISSUED, REISSUED, REVOKED
    }

    public void revoke() {
        this.status = CertificateStatus.REVOKED;
    }

    public void reissue() {
        this.status = CertificateStatus.REISSUED;
    }
}
