package com.sleekydz86.support.doctortreatment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorTreatmentResponse {

    private Long doctorTreatmentId;
    private Long patientNo;
    private String patientName;
    private Long userId;
    private String doctorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime doctorTreatmentStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime doctorTreatmentEnd;

    private Long treatmentDuration;
}

