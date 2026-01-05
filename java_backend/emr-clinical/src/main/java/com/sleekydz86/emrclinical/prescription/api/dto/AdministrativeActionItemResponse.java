package com.sleekydz86.emrclinical.prescription.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeActionItemResponse {

    @JsonProperty("ADM_DISPS_SEQ")
    private String admDispsSeq;

    @JsonProperty("ENTP_NAME")
    private String entpName;

    @JsonProperty("ADDR")
    private String addr;

    @JsonProperty("ENTP_SEQ")
    private String entpSeq;

    @JsonProperty("ITEM_NAME")
    private String itemName;

    @JsonProperty("BEF_APPLY_LAW")
    private String befApplyLaw;

    @JsonProperty("EXPOSE_CONT")
    private String exposeCont;

    @JsonProperty("ADM_DISPS_NAME")
    private String admDispsName;

    @JsonProperty("LAST_SETTLE_DATE")
    private String lastSettleDate;

    @JsonProperty("ITEM_SEQ")
    private String itemSeq;

    @JsonProperty("RLS_END_DATE")
    private String rlsEndDate;
}
