package com.sleekydz86.support.examination.journal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationResultRegisterRequest {

    @NotNull(message = "검사 ID는 필수 값입니다.")
    private Long examinationId;

    @NotNull(message = "환자 번호는 필수 값입니다.")
    private Long patientNo;

    @NotNull(message = "진료 ID는 필수 값입니다.")
    private Long treatmentId;

    @NotNull(message = "검사 날짜는 필수 값입니다.")
    private LocalDate examinationDate;

    private String examinationResult;

    private Boolean examinationNormal;

    private String examinationNotes;
}

