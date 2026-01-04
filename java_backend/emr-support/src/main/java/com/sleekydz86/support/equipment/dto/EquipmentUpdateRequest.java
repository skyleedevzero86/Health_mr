package com.sleekydz86.support.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentUpdateRequest {

    private String equipmentName;
    private String equipmentProductNumber;
    private String equipmentManufacturer;
    private String equipmentLocation;
    private String equipmentState;
    private LocalDate equipmentSchedule;
}
