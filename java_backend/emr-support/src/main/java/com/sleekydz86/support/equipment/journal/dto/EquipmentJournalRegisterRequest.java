package com.sleekydz86.support.equipment.journal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentJournalRegisterRequest {

    @NotNull(message = "장비 ID는 필수 값입니다.")
    private Long equipmentId;

    @NotNull(message = "사용자 ID는 필수 값입니다.")
    private Long userId;

    @NotNull(message = "점검 날짜는 필수 값입니다.")
    private LocalDate equipmentInspectionDate;

    private String equipmentInspectionResult;
    private String equipmentInspectionRecords;
    private String equipmentInspectionNotes;
}



