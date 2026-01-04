package com.sleekydz86.support.equipment.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentJournalUpdateRequest {

    private LocalDate equipmentInspectionDate;
    private String equipmentInspectionResult;
    private String equipmentInspectionRecords;
    private String equipmentInspectionNotes;
}

