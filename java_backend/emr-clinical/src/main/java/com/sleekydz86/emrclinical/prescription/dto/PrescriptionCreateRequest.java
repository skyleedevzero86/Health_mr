package com.sleekydz86.emrclinical.prescription.dto;

import com.sleekydz86.emrclinical.types.PrescriptionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PrescriptionCreateRequest {

    @NotNull(message = "진료 ID는 필수입니다.")
    private Long treatmentId;

    @NotNull(message = "환자 번호는 필수입니다.")
    private Long patientNo;

    @NotNull(message = "처방 의사 ID는 필수입니다.")
    private Long prescriptionDocId;

    @NotNull(message = "처방 유형은 필수입니다.")
    private PrescriptionType prescriptionType;

    private String prescriptionMemo;

    @Valid
    private List<PrescriptionItemCreateRequest> prescriptionItems;
}

