package com.sleekydz86.support.holiday.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HolidayResponse {

    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate holidayDate;
    private Boolean holidayNational;
    private String holidayReason;
}

