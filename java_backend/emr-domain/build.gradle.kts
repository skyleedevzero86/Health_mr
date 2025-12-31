plugins {
    `java`
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":emr-core"))
    implementation("org.modelmapper:modelmapper:3.1.1")
}

