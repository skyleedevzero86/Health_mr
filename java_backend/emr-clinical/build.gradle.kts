plugins {
    `java-library`
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    application
}

application {
    mainClass.set("com.sleekydz86.emrclinical.EmrClinicalApplication")
}

tasks.named("bootJar") {
    enabled = true
}

tasks.named("jar") {
    enabled = true
}

dependencies {
    implementation(project(":emr-core"))
    implementation(project(":emr-domain"))
    implementation("org.modelmapper:modelmapper:3.1.1")
}
