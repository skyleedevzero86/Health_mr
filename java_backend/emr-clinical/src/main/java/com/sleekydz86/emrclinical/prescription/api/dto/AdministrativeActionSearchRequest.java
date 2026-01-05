package com.sleekydz86.emrclinical.prescription.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeActionSearchRequest {

    private String entpName;

    private String itemName;

    private String itemSeq;

    private String order;

    private Integer pageNo;

    private Integer numOfRows;

    private String type;
}
