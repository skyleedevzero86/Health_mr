package com.sleekydz86.emrclinical.prescription.api;

import com.sleekydz86.emrclinical.prescription.api.dto.AdministrativeActionApiResponse;
import com.sleekydz86.emrclinical.prescription.api.dto.AdministrativeActionSearchRequest;
import reactor.core.publisher.Mono;

public interface AdministrativeActionApiClient {

    Mono<AdministrativeActionApiResponse> searchAdministrativeActions(AdministrativeActionSearchRequest request);

    Mono<AdministrativeActionApiResponse> getActionsByItemSeq(String itemSeq);

    Mono<AdministrativeActionApiResponse> getActionsByItemName(String itemName);

    Mono<AdministrativeActionApiResponse> getActionsByEntpName(String entpName);
}
