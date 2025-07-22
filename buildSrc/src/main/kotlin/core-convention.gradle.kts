@file:Suppress("UnstableApiUsage")


plugins {
    java
}



repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.3"))
    implementation(platform("io.ktor:ktor-bom:3.2.1"))
    implementation(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:2025.7.8"))


    compileOnly("org.springframework.boot:spring-boot-configuration-processor:3.5.3")
//    "kapt"("org.springframework.boot:spring-boot-configuration-processor:3.4.3")
}