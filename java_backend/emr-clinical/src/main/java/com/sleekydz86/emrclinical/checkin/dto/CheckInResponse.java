package com.sleekydz86.emrclinical.checkin.dto;

import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.types.CheckInStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CheckInResponse {

    private Long checkInId;
    private Long patientNo;
    private String patientName;
    private Long userId;
    private String userName;
    private LocalDateTime checkInDate;
    private CheckInStatus checkInStatus;
    private String checkInComment;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    public static CheckInResponse from(CheckInEntity entity) {
        return CheckInResponse.builder()
                .checkInId(entity.getCheckInId())
                .patientNo(entity.getPatientEntity().getPatientNoValue())
                .patientName(entity.getPatientEntity().getPatientName())
                .userId(entity.getUserEntity() != null ? entity.getUserEntity().getId() : null)
                .userName(entity.getUserEntity() != null ? entity.getUserEntity().getName() : null)
                .checkInDate(entity.getCheckInDate())
                .checkInStatus(entity.getCheckInStatus())
                .checkInComment(entity.getCheckInComment())
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }
}
