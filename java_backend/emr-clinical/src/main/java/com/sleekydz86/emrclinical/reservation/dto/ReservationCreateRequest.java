package com.sleekydz86.emrclinical.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationCreateRequest {

    @NotNull(message = "환자 번호는 필수입니다.")
    private Long patientNo;

    @NotNull(message = "예약 날짜/시간은 필수입니다.")
    @Future(message = "예약 날짜/시간은 미래 날짜만 가능합니다.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime reservationDate;

    private Long userId;
}

