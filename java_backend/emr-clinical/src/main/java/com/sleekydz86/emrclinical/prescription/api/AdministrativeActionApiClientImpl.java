package com.sleekydz86.emrclinical.prescription.api;

import com.sleekydz86.emrclinical.prescription.api.dto.AdministrativeActionApiResponse;
import com.sleekydz86.emrclinical.prescription.api.dto.AdministrativeActionSearchRequest;
import com.sleekydz86.emrclinical.prescription.api.exception.AdministrativeActionApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdministrativeActionApiClientImpl implements AdministrativeActionApiClient {

    private final WebClient administrativeActionWebClient;

    @Value("${drug-administrative-action.api.service-key}")
    private String serviceKey;

    @Value("${drug-administrative-action.api.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${drug-administrative-action.api.retry.delay:1000}")
    private long retryDelay;

    @Override
    public Mono<AdministrativeActionApiResponse> searchAdministrativeActions(
            AdministrativeActionSearchRequest request) {
        try {
            String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

            return administrativeActionWebClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/getMdcinExaathrList04")
                                .queryParam("serviceKey", encodedServiceKey)
                                .queryParam("type", request.getType() != null ? request.getType() : "json")
                                .queryParam("order", request.getOrder() != null ? request.getOrder() : "Y");

                        if (request.getEntpName() != null && !request.getEntpName().isBlank()) {
                            uriBuilder.queryParam("entp_name", request.getEntpName());
                        }
                        if (request.getItemName() != null && !request.getItemName().isBlank()) {
                            uriBuilder.queryParam("item_name", request.getItemName());
                        }
                        if (request.getItemSeq() != null && !request.getItemSeq().isBlank()) {
                            uriBuilder.queryParam("item_seq", request.getItemSeq());
                        }
                        if (request.getPageNo() != null) {
                            uriBuilder.queryParam("pageNo", request.getPageNo());
                        }
                        if (request.getNumOfRows() != null) {
                            uriBuilder.queryParam("numOfRows", request.getNumOfRows());
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, response -> {
                        log.error("Administrative Action API 4xx Error: {}", response.statusCode());
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new AdministrativeActionApiException(
                                        "의약품 행정처분 API 요청 실패: " + response.statusCode() + " - " + body)));
                    })
                    .onStatus(HttpStatus::is5xxServerError, response -> {
                        log.error("Administrative Action API 5xx Error: {}", response.statusCode());
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new AdministrativeActionApiException(
                                        "의약품 행정처분 API 서버 오류: " + response.statusCode() + " - " + body)));
                    })
                    .bodyToMono(AdministrativeActionApiResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .retryWhen(Retry.fixedDelay(maxRetryAttempts, Duration.ofMillis(retryDelay))
                            .filter(throwable -> throwable instanceof WebClientResponseException
                                    && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                    .doOnError(error -> log.error("Administrative Action API 호출 실패", error))
                    .doOnSuccess(response -> {
                        if (response != null && response.getResponse() != null) {
                            log.debug("Administrative Action API 호출 성공: resultCode={}",
                                    response.getResponse().getHeader() != null
                                            ? response.getResponse().getHeader().getResultCode()
                                            : "N/A");
                        }
                    });
        } catch (Exception e) {
            log.error("Administrative Action API 요청 생성 실패", e);
            return Mono.error(new AdministrativeActionApiException("의약품 행정처분 API 요청 생성 실패: " + e.getMessage(), e));
        }
    }

    @Override
    public Mono<AdministrativeActionApiResponse> getActionsByItemSeq(String itemSeq) {
        if (itemSeq == null || itemSeq.isBlank()) {
            return Mono.error(new AdministrativeActionApiException("품목기준코드는 필수입니다."));
        }

        AdministrativeActionSearchRequest request = AdministrativeActionSearchRequest.builder()
                .itemSeq(itemSeq)
                .order("Y")
                .pageNo(1)
                .numOfRows(10)
                .type("json")
                .build();

        return searchAdministrativeActions(request);
    }

    @Override
    public Mono<AdministrativeActionApiResponse> getActionsByItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return Mono.error(new AdministrativeActionApiException("제품명은 필수입니다."));
        }

        AdministrativeActionSearchRequest request = AdministrativeActionSearchRequest.builder()
                .itemName(itemName)
                .order("Y")
                .pageNo(1)
                .numOfRows(10)
                .type("json")
                .build();

        return searchAdministrativeActions(request);
    }

    @Override
    public Mono<AdministrativeActionApiResponse> getActionsByEntpName(String entpName) {
        if (entpName == null || entpName.isBlank()) {
            return Mono.error(new AdministrativeActionApiException("업체명은 필수입니다."));
        }

        AdministrativeActionSearchRequest request = AdministrativeActionSearchRequest.builder()
                .entpName(entpName)
                .order("Y")
                .pageNo(1)
                .numOfRows(10)
                .type("json")
                .build();

        return searchAdministrativeActions(request);
    }
}
