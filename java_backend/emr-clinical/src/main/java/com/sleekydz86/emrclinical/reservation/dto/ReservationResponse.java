package com.sleekydz86.emrclinical.reservation.dto;

import com.sleekydz86.emrclinical.reservation.entity.ReservationEntity;
import com.sleekydz86.emrclinical.types.ReservationStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationResponse {

    private Long reservationId;
    private Long patientNo;
    private String patientName;
    private Long userId;
    private String userName;
    private LocalDateTime reservationDate;
    private ReservationStatus reservationStatus;
    private String reservationYn;
    private LocalDateTime reservationChangeDate;
    private String reservationChangeCause;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    public static ReservationResponse from(ReservationEntity entity) {
        return ReservationResponse.builder()
                .reservationId(entity.getReservationId())
                .patientNo(entity.getPatientEntity().getPatientNo())
                .patientName(entity.getPatientEntity().getPatientName())
                .userId(entity.getUserEntity() != null ? entity.getUserEntity().getId() : null)
                .userName(entity.getUserEntity() != null ? entity.getUserEntity().getName() : null)
                .reservationDate(entity.getReservationDate())
                .reservationStatus(entity.getReservationStatus())
                .reservationYn(entity.getReservationYn())
                .reservationChangeDate(entity.getReservationChangeDate())
                .reservationChangeCause(entity.getReservationChangeCause())
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }
}

