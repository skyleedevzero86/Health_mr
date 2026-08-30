package com.sleekydz86.fhir.web;

import ca.uhn.fhir.context.FhirContext;
import com.sleekydz86.fhir.exception.FhirResourceNotFoundException;
import com.sleekydz86.fhir.exception.InvalidFhirSearchException;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = FhirController.class)
public class FhirExceptionHandler {
    private final FhirContext context;

    public FhirExceptionHandler(FhirContext context) {
        this.context = context;
    }

    @ExceptionHandler(FhirResourceNotFoundException.class)
    public ResponseEntity<String> notFound(FhirResourceNotFoundException exception) {
        return outcome(HttpStatus.NOT_FOUND, OperationOutcome.IssueType.NOTFOUND, exception.getMessage());
    }

    @ExceptionHandler({InvalidFhirSearchException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<String> invalidRequest(Exception exception) {
        return outcome(HttpStatus.BAD_REQUEST, OperationOutcome.IssueType.INVALID, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> internalError(Exception exception) {
        return outcome(HttpStatus.INTERNAL_SERVER_ERROR, OperationOutcome.IssueType.EXCEPTION,
                "The FHIR request could not be completed");
    }

    private ResponseEntity<String> outcome(HttpStatus status, OperationOutcome.IssueType type, String message) {
        OperationOutcome resource = new OperationOutcome();
        resource.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR).setCode(type).setDiagnostics(message);
        return ResponseEntity.status(status).contentType(MediaType.parseMediaType(FhirController.FHIR_JSON))
                .body(context.newJsonParser().encodeResourceToString(resource));
    }
}
