package com.sleekydz86.support.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRegisterRequest {

    @NotBlank(message = "장비명은 필수 값입니다.")
    private String equipmentName;

    @NotBlank(message = "제품번호는 필수 값입니다.")
    private String equipmentProductNumber;

    @NotBlank(message = "제조사는 필수 값입니다.")
    private String equipmentManufacturer;

    @NotBlank(message = "장비 위치는 필수 값입니다.")
    private String equipmentLocation;

    @NotBlank(message = "장비 상태는 필수 값입니다.")
    private String equipmentState;

    private LocalDate equipmentSchedule;
}

