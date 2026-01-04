package com.sleekydz86.support.examination.journal.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BloodBankRegisterRequest {

    @NotNull(message = "검사 ID는 필수 값입니다.")
    private Long examinationId;

    @NotNull(message = "환자 번호는 필수 값입니다.")
    private Long patientNo;

    @NotNull(message = "진료 ID는 필수 값입니다.")
    private Long treatmentId;

    @NotNull(message = "사용자 ID는 필수 값입니다.")
    private Long userId;

    @NotNull(message = "검사 시간은 필수 값입니다.")
    private LocalDateTime examinationTime;

    private String bloodType;
}

