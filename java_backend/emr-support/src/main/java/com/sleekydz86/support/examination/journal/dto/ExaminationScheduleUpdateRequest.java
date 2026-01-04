package com.sleekydz86.support.examination.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationScheduleUpdateRequest {

    private LocalDate examinationDate;
}

