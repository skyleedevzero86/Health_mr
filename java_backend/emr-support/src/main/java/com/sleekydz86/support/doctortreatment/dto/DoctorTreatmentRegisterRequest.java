package com.sleekydz86.support.doctortreatment.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorTreatmentRegisterRequest {

    @NotNull(message = "환자 번호는 필수 값입니다.")
    private Long patientNo;

    @NotNull(message = "의사 ID는 필수 값입니다.")
    private Long userId;

    @NotNull(message = "진료 시작 시간은 필수 값입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime doctorTreatmentStart;

    @NotNull(message = "진료 종료 시간은 필수 값입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime doctorTreatmentEnd;
}

