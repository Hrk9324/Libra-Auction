plugins {
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("java")
}

group = "io.github.guennhatking"
version = "0.0.1"
description = "An Online Auction System"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val mapstructVersion = "1.6.3"
val cloudinaryVersion = "2.0.0"

dependencies {
    // CORE WEB
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // DATABASE
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    // SECURITY + AUTH
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // REALTIME
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // CACHE
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // EMAIL
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // LIQUIDBASE
    implementation("org.springframework.boot:spring-boot-starter-liquibase")

    // SWAGGER / OPENAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
    implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.15")

    // CLOUDINARY
    implementation("com.cloudinary:cloudinary-http5:${cloudinaryVersion}")

    // MAPPING
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")

    // TEST
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}