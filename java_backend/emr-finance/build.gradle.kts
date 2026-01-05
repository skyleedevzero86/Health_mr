plugins {
    `java-library`
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    application
}

application {
    mainClass.set("com.sleekydz86.finance.EmrFinanceApplication")
}

tasks.named("bootJar") {
    enabled = true
}

tasks.named("jar") {
    enabled = false
}

dependencies {
    implementation(project(":emr-core"))
    implementation(project(":emr-domain"))
    implementation(project(":emr-clinical"))
    implementation("org.modelmapper:modelmapper:3.1.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
}
