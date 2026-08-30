plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

dependencies {
    implementation(project(":emr-core"))
    implementation(project(":emr-domain"))
    implementation(project(":emr-clinical"))
    implementation(project(":emr-support"))

    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:8.10.1")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:8.10.1")
}
