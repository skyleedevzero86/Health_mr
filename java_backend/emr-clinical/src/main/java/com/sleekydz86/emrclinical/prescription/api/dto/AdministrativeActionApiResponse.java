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
public class AdministrativeActionApiResponse {

    @JsonProperty("response")
    private AdministrativeActionResponse response;
}
