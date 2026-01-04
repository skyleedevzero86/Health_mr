package com.sleekydz86.support.equipment.journal.dto;

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
public class EquipmentJournalResponse {

    private Long equipmentJournalId;
    private Long equipmentId;
    private String equipmentName;
    private Long userId;
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate equipmentInspectionDate;

    private String equipmentInspectionResult;
    private String equipmentInspectionRecords;
    private String equipmentInspectionNotes;
}

