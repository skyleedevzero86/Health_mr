package com.sleekydz86.support.recess.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecessResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String role;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime recessStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime recessEnd;
    private String recessReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}

