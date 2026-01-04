package com.sleekydz86.support.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentResponse {

    private Long equipmentId;
    private String equipmentName;
    private String equipmentProductNumber;
    private String equipmentManufacturer;
    private String equipmentLocation;
    private String equipmentState;
    private LocalDate equipmentSchedule;
}

