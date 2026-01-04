package com.sleekydz86.support.recess.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.domain.user.type.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecessRequest {
    private Long userId;
    private RoleType role;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime recessStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime recessEnd;
    private String recessReason;
}

