package com.sleekydz86.emrclinical.prescription.dto;

import com.sleekydz86.emrclinical.prescription.entity.PrescriptionEntity;
import com.sleekydz86.emrclinical.types.PrescriptionStatus;
import com.sleekydz86.emrclinical.types.PrescriptionType;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PrescriptionDetailResponse {

    private Long prescriptionId;
    private Long treatmentId;
    private Long patientNo;
    private String patientName;
    private Long prescriptionDocId;
    private String prescriptionDocName;
    private String prescriptionDocEmail;
    private String prescriptionDocTel;
    private PrescriptionStatus prescriptionStatus;
    private PrescriptionType prescriptionType;
    private LocalDateTime prescriptionDate;
    private String prescriptionMemo;
    private List<PrescriptionItemResponse> prescriptionItems;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    public static PrescriptionDetailResponse from(PrescriptionEntity entity) {
        return PrescriptionDetailResponse.builder()
                .prescriptionId(entity.getPrescriptionId())
                .treatmentId(entity.getTreatmentEntity().getTreatmentId())
                .patientNo(entity.getPatientEntity().getPatientNo())
                .patientName(entity.getPatientEntity().getPatientName())
                .prescriptionDocId(entity.getPrescriptionDoc().getId())
                .prescriptionDocName(entity.getPrescriptionDoc().getName())
                .prescriptionDocEmail(entity.getPrescriptionDoc().getEmail())
                .prescriptionDocTel(entity.getPrescriptionDoc().getTelNum())
                .prescriptionStatus(entity.getPrescriptionStatus())
                .prescriptionType(entity.getPrescriptionType())
                .prescriptionDate(entity.getPrescriptionDate())
                .prescriptionMemo(entity.getPrescriptionMemo())
                .prescriptionItems(entity.getPrescriptionItems().stream()
                        .map(item -> PrescriptionItemResponse.builder()
                                .prescriptionItemId(item.getPrescriptionItemId())
                                .drugCode(item.getDrugCode())
                                .drugName(item.getDrugName())
                                .dosage(item.getDosage())
                                .dose(item.getDose())
                                .frequency(item.getFrequency())
                                .days(item.getDays())
                                .totalQuantity(item.getTotalQuantity())
                                .unit(item.getUnit())
                                .specialNote(item.getSpecialNote())
                                .build())
                        .collect(Collectors.toList()))
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }
}
