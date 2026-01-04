package com.sleekydz86.support.examination.journal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationResultResponse {

    private Long examinationResultId;
    private Long examinationId;
    private String examinationName;
    private Long patientNo;
    private String patientName;
    private Long treatmentId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate examinationDate;

    private String examinationResult;
    private Boolean examinationNormal;
    private String examinationNotes;
}

