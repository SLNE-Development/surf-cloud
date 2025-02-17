plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    `core-convention`
}

dependencies {
    api(project(":surf-cloud-api:surf-cloud-api-common"))

    // https://mvnrepository.com/artifact/jakarta.persistence/jakarta.persistence-api
    api("jakarta.persistence:jakarta.persistence-api")

    // DB
    val exposedVersion = "0.59.0"
    // https://mvnrepository.com/artifact/org.springframework.data/spring-data-jpa
    api("org.springframework.data:spring-data-jpa")
    api("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")
    api("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    api("org.apache.maven:maven-impl:4.0.0-rc-2")
    api("org.apache.maven.resolver:maven-resolver-supplier-mvn4:2.0.5")
    api("org.jline:jline:3.26.3")
    api("com.mojang:brigadier:1.3.10")
    api("net.minecrell:terminalconsoleappender:1.3.0")

    // Ktor
    api("io.ktor:ktor-server-core-jvm")
    api("io.ktor:ktor-server-netty")
    api("io.ktor:ktor-server-core")
    api("io.ktor:ktor-server-html-builder")
    api("org.jetbrains.kotlin-wrappers:kotlin-css")
    api("io.ktor:ktor-server-websockets")
    api("io.ktor:ktor-serialization-kotlinx-json")

    api("org.mongodb:bson-kotlinx:5.3.0")
}