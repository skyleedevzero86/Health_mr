package com.sleekydz86.emrclinical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.sleekydz86")
public class EmrClinicalApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmrClinicalApplication.class, args);
    }

}
