package com.sleekydz86.emrclinical.prescription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PrescriptionUpdateRequest {

    @Size(max = 2000, message = "처방 메모는 최대 2000자까지 가능합니다.")
    private String prescriptionMemo;

    @Valid
    private List<PrescriptionItemCreateRequest> prescriptionItems;
}
