package com.sleekydz86.fhir.exception;

public class FhirResourceNotFoundException extends RuntimeException {
    public FhirResourceNotFoundException(String resourceType, Long id) {
        super(resourceType + "/" + id + " was not found");
    }
}
