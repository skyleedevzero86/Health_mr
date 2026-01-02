package com.sleekydz86.emrclinical.treatment.inpatient;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.types.TreatmentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "In_Treatments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InTreatmentEntity extends BaseEntity {

    @Id
    private Long id;

    @MapsId
    @OneToOne(targetEntity = TreatmentEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    private TreatmentEntity treatmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkIn_id", referencedColumnName = "checkIn_id", nullable = false)
    private CheckInEntity checkInEntity;

    @Column(name = "treatment_status", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TreatmentStatus treatmentStatus = TreatmentStatus.PENDING;
}

