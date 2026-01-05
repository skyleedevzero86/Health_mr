package com.sleekydz86.support.disability.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CareInstitutionSearchRequest {
    private String serviceType;
    private String sido;
    private String institutionType;
    private int page;
    private int size;
}

