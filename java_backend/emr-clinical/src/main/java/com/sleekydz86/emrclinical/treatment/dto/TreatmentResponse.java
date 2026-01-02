package com.sleekydz86.emrclinical.treatment.dto;

import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.types.TreatmentStatus;
import com.sleekydz86.emrclinical.types.TreatmentType;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class TreatmentResponse {

    private Long treatmentId;
    private Long patientNo;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private Long checkInId;
    private TreatmentType treatmentType;
    private TreatmentStatus treatmentStatus;
    private LocalDateTime treatmentDate;
    private LocalDateTime treatmentStartTime;
    private LocalDateTime treatmentEndTime;
    private String treatmentComment;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    public static TreatmentResponse from(TreatmentEntity entity) {
        return TreatmentResponse.builder()
                .treatmentId(entity.getTreatmentId())
                .patientNo(entity.getCheckInEntity() != null ?
                        entity.getCheckInEntity().getPatientEntity().getPatientNoValue() : null)
                .patientName(entity.getCheckInEntity() != null ?
                        entity.getCheckInEntity().getPatientEntity().getPatientName() : null)
                .doctorId(entity.getTreatmentDoc().getId())
                .doctorName(entity.getTreatmentDoc().getName())
                .departmentId(entity.getDepartmentEntity() != null ? entity.getDepartmentEntity().getId() : null)
                .departmentName(entity.getDepartmentEntity() != null ? entity.getDepartmentEntity().getName() : null)
                .checkInId(entity.getCheckInEntity() != null ? entity.getCheckInEntity().getCheckInId() : null)
                .treatmentType(entity.getTreatmentType())
                .treatmentStatus(entity.getTreatmentStatus())
                .treatmentDate(entity.getTreatmentDate())
                .treatmentStartTime(entity.getTreatmentStartTime())
                .treatmentEndTime(entity.getTreatmentEndTime())
                .treatmentComment(entity.getTreatmentComment())
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }
}

