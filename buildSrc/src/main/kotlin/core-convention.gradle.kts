@file:Suppress("UnstableApiUsage")

plugins {
    java
//    `java-library`
//    `maven-publish`

//    kotlin("jvm")
//    kotlin("kapt")
//    kotlin("plugin.spring")
//    kotlin("plugin.jpa")
//    kotlin("plugin.lombok")

//    id("org.hibernate.build.maven-repo-auth")
//    id("com.gradleup.shadow")
}

group = "dev.slne.surf.cloud"
version = "1.21.1-1.0.0-SNAPSHOT"

//extensions.configure<KotlinJvmProjectExtension> {
//    jvmToolchain(21)
//    compilerOptions {
//        freeCompilerArgs = listOf("-Xjsr305=strict")
//    }
//}

repositories {
//    gradlePluginPortal()
    mavenCentral()
//    maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
//    maven("https://repo.slne.dev/repository/maven-snapshots/") { name = "maven-snapshots" }
//    maven("https://repo.slne.dev/repository/maven-proxy/") { name = "maven-proxy" }
//    maven("https://repo.glaremasters.me/repository/public/")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.6"))
    // developmentOnly("org.springframework.boot:spring-boot-devtools")

//    compileOnly("dev.slne.surf:surf-api-core-api:1.21+")
//    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")

    implementation("org.springframework.boot:spring-boot-configuration-processor:3.3.6")
    "kapt"("org.springframework.boot:spring-boot-configuration-processor:3.3.6")
//
//    implementation("com.google.auto.service:auto-service:1.1.1")
//    kapt("com.google.auto.service:auto-service:1.1.1")
}

//publishing {
//    repositories {
//        maven("https://repo.slne.dev/repository/maven-snapshots/") { name = "maven-snapshots" }
//    }
//
//    publications.create<MavenPublication>("maven") {
//        from(components["java"])
//    }
//}

//configurations {
//    compileOnly {
//        extendsFrom(configurations.annotationProcessor.get())
//    }
//}
//
//tasks {
//    shadowJar {
//        mergeServiceFiles {
//            setPath("META-INF")
//            exclude("META-INF/MANIFEST.MF")
//        }
//
//        isZip64 = true
//    }
//
//    jar {
//        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    }
//}
//
//allOpen {
//    annotation("jakarta.persistence.Entity")
//    annotation("jakarta.persistence.MappedSuperclass")
//    annotation("jakarta.persistence.Embeddable")
//}