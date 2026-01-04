package com.sleekydz86.support.examination.journal.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BloodBankUpdateRequest {

    private LocalDateTime examinationTime;
    private String bloodType;
}

