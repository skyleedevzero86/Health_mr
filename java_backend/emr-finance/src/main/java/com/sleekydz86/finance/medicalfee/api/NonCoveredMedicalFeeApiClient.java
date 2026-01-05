package com.sleekydz86.finance.medicalfee.api;

import com.sleekydz86.finance.medicalfee.api.dto.NonPaymentItemHospDtlResponse;
import com.sleekydz86.finance.medicalfee.api.dto.NonPaymentItemHospSummaryResponse;
import com.sleekydz86.finance.medicalfee.api.exception.NonCoveredMedicalFeeApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class NonCoveredMedicalFeeApiClient {

    private final WebClient nonCoveredMedicalFeeWebClient;

    @Value("${non-covered-medical-fee.api.service-key}")
    private String serviceKey;

    public Mono<NonPaymentItemHospDtlResponse> getNonPaymentItemHospDtlList(
            String ykiho,
            String clCd,
            String sidoCd,
            String sgguCd,
            String yadmNm,
            int pageNo,
            int numOfRows
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/getNonPaymentItemHospDtlList")
                .queryParam("serviceKey", encodeServiceKey(serviceKey))
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows);

        if (ykiho != null && !ykiho.isBlank()) {
            builder.queryParam("ykiho", ykiho);
        }
        if (clCd != null && !clCd.isBlank()) {
            builder.queryParam("clCd", clCd);
        }
        if (sidoCd != null && !sidoCd.isBlank()) {
            builder.queryParam("sidoCd", sidoCd);
        }
        if (sgguCd != null && !sgguCd.isBlank()) {
            builder.queryParam("sgguCd", sgguCd);
        }
        if (yadmNm != null && !yadmNm.isBlank()) {
            builder.queryParam("yadmNm", yadmNm);
        }

        String uri = builder.toUriString();

        return nonCoveredMedicalFeeWebClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("API 호출 실패: Status={}, Body={}", response.statusCode(), body);
                                    return Mono.error(new NonCoveredMedicalFeeApiException(
                                            String.valueOf(response.statusCode().value()),
                                            "API 호출 실패: " + response.statusCode()
                                    ));
                                }))
                .bodyToMono(NonPaymentItemHospDtlResponse.class)
                .doOnNext(response -> {
                    if (response.getHeader() != null && !"00".equals(response.getHeader().getResultCode())) {
                        String errorCode = response.getHeader().getResultCode();
                        String errorMsg = response.getHeader().getResultMsg();
                        log.error("API 오류 응답: Code={}, Message={}", errorCode, errorMsg);
                        throw NonCoveredMedicalFeeApiException.fromErrorCode(errorCode);
                    }
                })
                .onErrorMap(throwable -> {
                    if (throwable instanceof NonCoveredMedicalFeeApiException) {
                        return throwable;
                    }
                    log.error("API 호출 중 예외 발생", throwable);
                    return new NonCoveredMedicalFeeApiException("99", "API 호출 중 예외 발생: " + throwable.getMessage(), throwable);
                });
    }

    public Mono<NonPaymentItemHospSummaryResponse> getNonPaymentItemHospList2(
            String itemCd,
            String clCd,
            String sidoCd,
            String sgguCd,
            String yadmNm,
            String searchWrd,
            int pageNo,
            int numOfRows
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/getNonPaymentItemHospList2")
                .queryParam("serviceKey", encodeServiceKey(serviceKey))
                .queryParam("itemCd", itemCd)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows);

        if (clCd != null && !clCd.isBlank()) {
            builder.queryParam("clCd", clCd);
        }
        if (sidoCd != null && !sidoCd.isBlank()) {
            builder.queryParam("sidoCd", sidoCd);
        }
        if (sgguCd != null && !sgguCd.isBlank()) {
            builder.queryParam("sgguCd", sgguCd);
        }
        if (yadmNm != null && !yadmNm.isBlank()) {
            builder.queryParam("yadmNm", yadmNm);
        }
        if (searchWrd != null && !searchWrd.isBlank()) {
            builder.queryParam("searchWrd", searchWrd);
        }

        String uri = builder.toUriString();

        return nonCoveredMedicalFeeWebClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("API 호출 실패: Status={}, Body={}", response.statusCode(), body);
                                    return Mono.error(new NonCoveredMedicalFeeApiException(
                                            String.valueOf(response.statusCode().value()),
                                            "API 호출 실패: " + response.statusCode()
                                    ));
                                }))
                .bodyToMono(NonPaymentItemHospSummaryResponse.class)
                .doOnNext(response -> {
                    if (response.getHeader() != null && !"00".equals(response.getHeader().getResultCode())) {
                        String errorCode = response.getHeader().getResultCode();
                        String errorMsg = response.getHeader().getResultMsg();
                        log.error("API 오류 응답: Code={}, Message={}", errorCode, errorMsg);
                        throw NonCoveredMedicalFeeApiException.fromErrorCode(errorCode);
                    }
                })
                .onErrorMap(throwable -> {
                    if (throwable instanceof NonCoveredMedicalFeeApiException) {
                        return throwable;
                    }
                    log.error("API 호출 중 예외 발생", throwable);
                    return new NonCoveredMedicalFeeApiException("99", "API 호출 중 예외 발생: " + throwable.getMessage(), throwable);
                });
    }

    private String encodeServiceKey(String key) {
        try {
            return URLEncoder.encode(key, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            log.error("ServiceKey 인코딩 실패", e);
            return key;
        }
    }
}

