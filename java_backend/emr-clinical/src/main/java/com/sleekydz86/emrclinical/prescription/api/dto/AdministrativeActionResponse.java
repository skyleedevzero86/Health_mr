package com.sleekydz86.emrclinical.prescription.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeActionResponse {

    @JsonProperty("header")
    private ResponseHeader header;

    @JsonProperty("body")
    private ResponseBody body;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseHeader {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseBody {
        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("totalCount")
        private Integer totalCount;

        @JsonProperty("items")
        private List<AdministrativeActionItemResponse> items;
    }

    public boolean isSuccess() {
        return header != null && "00".equals(header.getResultCode());
    }

    public List<AdministrativeActionItemResponse> getItems() {
        return body != null && body.getItems() != null ? body.getItems() : List.of();
    }
}
