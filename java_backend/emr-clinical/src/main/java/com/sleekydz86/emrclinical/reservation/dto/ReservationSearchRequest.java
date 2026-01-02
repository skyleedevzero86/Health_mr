package com.sleekydz86.emrclinical.reservation.dto;

import com.sleekydz86.emrclinical.types.ReservationStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class ReservationSearchRequest {

    private Long patientNo;
    private Long userId;
    private ReservationStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
}

