package com.sleekydz86.support.examination.journal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodBankResponse {

    private Long bloodBankId;
    private Long examinationId;
    private String examinationName;
    private Long patientNo;
    private String patientName;
    private Long treatmentId;
    private Long userId;
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime examinationTime;

    private String bloodType;
}

