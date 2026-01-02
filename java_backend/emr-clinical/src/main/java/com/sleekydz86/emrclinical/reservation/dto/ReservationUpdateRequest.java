package com.sleekydz86.emrclinical.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationUpdateRequest {

    @Future(message = "예약 날짜/시간은 미래 날짜만 가능합니다.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime newReservationDate;

    private String reservationYn;

    @Size(max = 500, message = "예약 변경 사유는 최대 500자까지 가능합니다.")
    private String reservationChangeCause;
}

