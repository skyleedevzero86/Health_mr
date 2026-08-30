package com.sleekydz86.fhir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.sleekydz86")
public class EmrFhirApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmrFhirApplication.class, args);
    }
}
