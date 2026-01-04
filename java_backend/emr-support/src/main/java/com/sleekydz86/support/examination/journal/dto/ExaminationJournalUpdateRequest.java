package com.sleekydz86.support.examination.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationJournalUpdateRequest {

    private LocalDateTime examinationTime;

    private Boolean examinationEquipmentUsage;

    private String examinationNotes;
}