plugins {
    `java-library`
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    application
}

application {
    mainClass.set("com.sleekydz86.support.EmrSupportApplication")
}

tasks.named("bootJar") {
    enabled = true
}

tasks.named("jar") {
    enabled = false
}

dependencies {
    // 프로젝트 모듈 의존성
    implementation(project(":emr-core"))
    implementation(project(":emr-domain"))
    implementation(project(":emr-clinical"))

    // DTO 변환
    implementation("org.modelmapper:modelmapper:3.1.1")

    // 테스트
    testImplementation("org.springframework.security:spring-security-test")
}
