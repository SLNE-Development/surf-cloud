@file:Suppress("UnstableApiUsage")


plugins {
    java
}



repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.6"))
    implementation(platform("io.ktor:ktor-bom:3.3.0"))
    implementation(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:2025.9.8"))


    compileOnly("org.springframework.boot:spring-boot-configuration-processor:3.5.6")
//    "kapt"("org.springframework.boot:spring-boot-configuration-processor:3.4.3")
}