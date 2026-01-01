plugins {
    `java-library`
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

tasks.named("bootJar") {
    enabled = false
}

tasks.named("jar") {
    enabled = true
}

dependencies {
    implementation(project(":emr-core"))
    implementation(project(":emr-domain"))
    implementation("org.modelmapper:modelmapper:3.1.1")
}
