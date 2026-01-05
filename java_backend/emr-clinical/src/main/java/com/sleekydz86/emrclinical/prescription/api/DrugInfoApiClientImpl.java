package com.sleekydz86.emrclinical.prescription.api;

import com.sleekydz86.emrclinical.prescription.api.dto.DrugInfoApiResponse;
import com.sleekydz86.emrclinical.prescription.api.dto.DrugInfoSearchRequest;
import com.sleekydz86.emrclinical.prescription.api.exception.DrugInfoApiException;
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
public class DrugInfoApiClientImpl implements DrugInfoApiClient {
    
    private final WebClient drugInfoWebClient;
    
    @Value("${drug-info.api.service-key}")
    private String serviceKey;
    
    @Value("${drug-info.api.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${drug-info.api.retry.delay:1000}")
    private long retryDelay;
    
    @Override
    public Mono<DrugInfoApiResponse> searchDrugInfo(DrugInfoSearchRequest request) {
        try {
            String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
            
            return drugInfoWebClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/getDrbEasyDrugList")
                                .queryParam("serviceKey", encodedServiceKey)
                                .queryParam("type", "json");
                        
                        if (request.getItemName() != null && !request.getItemName().isBlank()) {
                            uriBuilder.queryParam("itemName", request.getItemName());
                        }
                        if (request.getItemSeq() != null && !request.getItemSeq().isBlank()) {
                            uriBuilder.queryParam("itemSeq", request.getItemSeq());
                        }
                        if (request.getEntpName() != null && !request.getEntpName().isBlank()) {
                            uriBuilder.queryParam("entpName", request.getEntpName());
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
                        log.error("Drug Info API 4xx Error: {}", response.statusCode());
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new DrugInfoApiException(
                                        "의약품 정보 API 요청 실패: " + response.statusCode() + " - " + body)));
                    })
                    .onStatus(HttpStatus::is5xxServerError, response -> {
                        log.error("Drug Info API 5xx Error: {}", response.statusCode());
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new DrugInfoApiException(
                                        "의약품 정보 API 서버 오류: " + response.statusCode() + " - " + body)));
                    })
                    .bodyToMono(DrugInfoApiResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .retryWhen(Retry.fixedDelay(maxRetryAttempts, Duration.ofMillis(retryDelay))
                            .filter(throwable -> throwable instanceof WebClientResponseException
                                    && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                    .doOnError(error -> log.error("Drug Info API 호출 실패", error))
                    .doOnSuccess(response -> {
                        if (response != null && response.getResponse() != null) {
                            log.debug("Drug Info API 호출 성공: resultCode={}", 
                                    response.getResponse().getHeader() != null ? 
                                    response.getResponse().getHeader().getResultCode() : "N/A");
                        }
                    });
        } catch (Exception e) {
            log.error("Drug Info API 요청 생성 실패", e);
            return Mono.error(new DrugInfoApiException("의약품 정보 API 요청 생성 실패: " + e.getMessage(), e));
        }
    }
    
    @Override
    public Mono<DrugInfoApiResponse> getDrugInfoByItemSeq(String itemSeq) {
        if (itemSeq == null || itemSeq.isBlank()) {
            return Mono.error(new DrugInfoApiException("품목기준코드는 필수입니다."));
        }
        
        DrugInfoSearchRequest request = DrugInfoSearchRequest.builder()
                .itemSeq(itemSeq)
                .pageNo(1)
                .numOfRows(1)
                .build();
        
        return searchDrugInfo(request);
    }
    
    @Override
    public Mono<DrugInfoApiResponse> getDrugInfoByItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return Mono.error(new DrugInfoApiException("제품명은 필수입니다."));
        }
        
        DrugInfoSearchRequest request = DrugInfoSearchRequest.builder()
                .itemName(itemName)
                .pageNo(1)
                .numOfRows(10)
                .build();
        
        return searchDrugInfo(request);
    }
}

