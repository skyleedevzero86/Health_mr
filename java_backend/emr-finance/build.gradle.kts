plugins {
    `java-library`
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    application
}

application {
    mainClass.set("com.sleekydz86.finance.EmrFinanceApplication")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    archiveClassifier.set("boot")
}

tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    implementation(project(":emr-core"))
    implementation(project(":emr-domain"))
    implementation(project(":emr-clinical"))
    implementation("org.modelmapper:modelmapper:3.1.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.clickhouse:clickhouse-jdbc:0.8.6")
    testRuntimeOnly("com.h2database:h2")
}
