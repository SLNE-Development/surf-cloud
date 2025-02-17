@file:Suppress("UnstableApiUsage")


plugins {
    java
}

group = "dev.slne.surf.cloud"
version = "1.21.1-1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.6"))
    implementation(platform("io.ktor:ktor-bom:3.0.3"))
    implementation(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:2025.2.2"))


    compileOnly("org.springframework.boot:spring-boot-configuration-processor:3.3.6")
    "kapt"("org.springframework.boot:spring-boot-configuration-processor:3.3.6")
}