package com.sleekydz86.emrclinical.prescription.api;

import com.sleekydz86.emrclinical.prescription.api.dto.DrugInfoApiResponse;
import com.sleekydz86.emrclinical.prescription.api.dto.DrugInfoSearchRequest;
import reactor.core.publisher.Mono;

public interface DrugInfoApiClient {

    Mono<DrugInfoApiResponse> searchDrugInfo(DrugInfoSearchRequest request);

    Mono<DrugInfoApiResponse> getDrugInfoByItemSeq(String itemSeq);

    Mono<DrugInfoApiResponse> getDrugInfoByItemName(String itemName);
}
