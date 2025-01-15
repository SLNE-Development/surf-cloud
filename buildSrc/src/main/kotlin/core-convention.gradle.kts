@file:Suppress("UnstableApiUsage")


plugins {
    java
//    id("io.spring.dependency-management")
}

group = "dev.slne.surf.cloud"
version = "1.21.1-1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.6"))

    compileOnly("org.springframework.boot:spring-boot-configuration-processor:3.3.6")
    "kapt"("org.springframework.boot:spring-boot-configuration-processor:3.3.6")
}