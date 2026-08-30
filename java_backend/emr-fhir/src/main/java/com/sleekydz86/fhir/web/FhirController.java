package com.sleekydz86.fhir.web;

import ca.uhn.fhir.context.FhirContext;
import com.sleekydz86.fhir.service.FhirReadService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fhir")
public class FhirController {
    public static final String FHIR_JSON = "application/fhir+json";
    private final FhirReadService service;
    private final FhirContext context;

    public FhirController(FhirReadService service, FhirContext context) {
        this.service = service;
        this.context = context;
    }

    @GetMapping(value = "/Patient/{id}", produces = FHIR_JSON)
    public ResponseEntity<String> patient(@PathVariable Long id) { return response(service.patient(id)); }

    @GetMapping(value = "/Patient", produces = FHIR_JSON)
    public ResponseEntity<String> patients(@RequestParam(required = false) String name) {
        return response(service.patients(name));
    }

    @GetMapping(value = "/Practitioner/{id}", produces = FHIR_JSON)
    public ResponseEntity<String> practitioner(@PathVariable Long id) { return response(service.practitioner(id)); }

    @GetMapping(value = "/Practitioner", produces = FHIR_JSON)
    public ResponseEntity<String> practitioners(@RequestParam(required = false) String name) {
        return response(service.practitioners(name));
    }

    @GetMapping(value = "/Encounter/{id}", produces = FHIR_JSON)
    public ResponseEntity<String> encounter(@PathVariable Long id) { return response(service.encounter(id)); }

    @GetMapping(value = "/Encounter", produces = FHIR_JSON)
    public ResponseEntity<String> encounters(@RequestParam String subject) { return response(service.encounters(subject)); }

    @GetMapping(value = "/Observation/{id}", produces = FHIR_JSON)
    public ResponseEntity<String> observation(@PathVariable Long id) { return response(service.observation(id)); }

    @GetMapping(value = "/Observation", produces = FHIR_JSON)
    public ResponseEntity<String> observations(@RequestParam String subject, @RequestParam(required = false) String code) {
        return response(service.observations(subject, code));
    }

    @GetMapping(value = "/Condition/{id}", produces = FHIR_JSON)
    public ResponseEntity<String> condition(@PathVariable Long id) { return response(service.condition(id)); }

    @GetMapping(value = "/Condition", produces = FHIR_JSON)
    public ResponseEntity<String> conditions(@RequestParam String subject) { return response(service.conditions(subject)); }

    @GetMapping(value = "/MedicationRequest/{id}", produces = FHIR_JSON)
    public ResponseEntity<String> medicationRequest(@PathVariable Long id) { return response(service.medicationRequest(id)); }

    @GetMapping(value = "/MedicationRequest", produces = FHIR_JSON)
    public ResponseEntity<String> medicationRequests(@RequestParam String subject) { return response(service.medicationRequests(subject)); }

    private ResponseEntity<String> response(IBaseResource resource) {
        return ResponseEntity.ok().contentType(org.springframework.http.MediaType.parseMediaType(FHIR_JSON))
                .body(context.newJsonParser().encodeResourceToString(resource));
    }
}
