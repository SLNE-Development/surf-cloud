@file:Suppress("UnstableApiUsage")


plugins {
    java
}



repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.3"))
    implementation(platform("io.ktor:ktor-bom:3.0.3"))
    implementation(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:2025.2.2"))


    compileOnly("org.springframework.boot:spring-boot-configuration-processor:3.4.3")
//    "kapt"("org.springframework.boot:spring-boot-configuration-processor:3.4.3")
}