package com.sleekydz86.fhir.exception;

public class InvalidFhirSearchException extends RuntimeException {
    public InvalidFhirSearchException(String message) {
        super(message);
    }
}
