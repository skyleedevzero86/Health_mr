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
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-aop:4.0.0-M1")
    api("org.springframework.boot:spring-boot-starter-mail")
    api("org.springframework.boot:spring-boot-starter-data-redis")

    api("io.jsonwebtoken:jjwt-api:0.12.6")
    api("io.jsonwebtoken:jjwt-impl:0.12.6")
    api("io.jsonwebtoken:jjwt-jackson:0.12.6")

    api("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5")

    api("org.apache.poi:poi:5.2.5")
    api("org.apache.poi:poi-ooxml:5.2.5")

    api("org.apache.commons:commons-lang3:3.14.0")
    api("commons-io:commons-io:2.15.1")

    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}