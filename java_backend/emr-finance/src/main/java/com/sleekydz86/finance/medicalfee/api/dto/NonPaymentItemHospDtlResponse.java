package com.sleekydz86.finance.medicalfee.api.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@JacksonXmlRootElement(localName = "response")
public class NonPaymentItemHospDtlResponse {
    @JacksonXmlProperty(localName = "header")
    private ResponseHeader header;

    @JacksonXmlProperty(localName = "body")
    private ResponseBody body;

    @Getter
    @Setter
    public static class ResponseHeader {
        @JacksonXmlProperty(localName = "resultCode")
        private String resultCode;

        @JacksonXmlProperty(localName = "resultMsg")
        private String resultMsg;
    }

    @Getter
    @Setter
    public static class ResponseBody {
        @JacksonXmlProperty(localName = "items")
        private ItemsWrapper items;

        @JacksonXmlProperty(localName = "numOfRows")
        private Integer numOfRows;

        @JacksonXmlProperty(localName = "pageNo")
        private Integer pageNo;

        @JacksonXmlProperty(localName = "totalCount")
        private Integer totalCount;
    }

    @Getter
    @Setter
    public static class ItemsWrapper {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        private List<NonPaymentItemHospDtlItem> item;
    }

    @Getter
    @Setter
    public static class NonPaymentItemHospDtlItem {
        @JacksonXmlProperty(localName = "ykiho")
        private String ykiho;

        @JacksonXmlProperty(localName = "yadmNm")
        private String yadmNm;

        @JacksonXmlProperty(localName = "clCd")
        private String clCd;

        @JacksonXmlProperty(localName = "clCdNm")
        private String clCdNm;

        @JacksonXmlProperty(localName = "sidoCd")
        private String sidoCd;

        @JacksonXmlProperty(localName = "sidoCdNm")
        private String sidoCdNm;

        @JacksonXmlProperty(localName = "sgguCd")
        private String sgguCd;

        @JacksonXmlProperty(localName = "sgguCdNm")
        private String sgguCdNm;

        @JacksonXmlProperty(localName = "npayCd")
        private String npayCd;

        @JacksonXmlProperty(localName = "npayKorNm")
        private String npayKorNm;

        @JacksonXmlProperty(localName = "yadmNpayCdNm")
        private String yadmNpayCdNm;

        @JacksonXmlProperty(localName = "adtFrDd")
        private String adtFrDd;

        @JacksonXmlProperty(localName = "adtEndDd")
        private String adtEndDd;

        @JacksonXmlProperty(localName = "curAmt")
        private Long curAmt;
    }

    public List<NonPaymentItemHospDtlItem> getItems() {
        if (body != null && body.getItems() != null && body.getItems().getItem() != null) {
            return body.getItems().getItem();
        }
        return List.of();
    }

    public Integer getTotalCount() {
        return body != null ? body.getTotalCount() : 0;
    }
}

