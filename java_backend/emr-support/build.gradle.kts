plugins {
    `java-library`
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    application
}

application {
    mainClass.set("com.sleekydz86.support.EmrSupportApplication")
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
    testRuntimeOnly("com.h2database:h2")
}
