package com.sleekydz86.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.sleekydz86.domain.patient.service.generators.*;

@Configuration
public class PatientNumberGeneratorConfig {

    @Value("${patient.number.generation.strategy:MAX_VALUE}")
    private String strategy;

    @Bean
    @Primary
    public PatientNumberGenerator patientNumberGenerator(
            SequenceBasedGenerator sequenceBasedGenerator,
            MaxValueBasedGenerator maxValueBasedGenerator) {
        return switch (strategy.toUpperCase()) {
            case "SEQUENCE" -> sequenceBasedGenerator;
            case "MAX_VALUE" -> maxValueBasedGenerator;
            default -> maxValueBasedGenerator;
        };
    }
}
