package com.sleekydz86.emrclinical.diagnosis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KcdCodeSearchRequest {

    private String keyword;
    private String category;
}
