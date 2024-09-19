import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `java-library`
    `maven-publish`
//    id("io.spring.dependency-management") version "1.1.6"
    id("org.hibernate.build.maven-repo-auth") version "3.0.4" apply false
    id("io.github.goooler.shadow") version "8.1.8" apply false
    id("io.freefair.lombok") version "8.10" apply false
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}



allprojects {
    apply(plugin = "java")
    apply(plugin = "org.gradle.java-library")
    apply(plugin = "io.github.goooler.shadow")
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.hibernate.build.maven-repo-auth")
    apply(plugin = "io.freefair.lombok")


    group = "dev.slne.surf.cloud"
    version = "1.21.1-1.0.0-SNAPSHOT"

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
        maven("https://repo.slne.dev/repository/maven-snapshots/") { name = "maven-snapshots" }
        maven("https://repo.slne.dev/repository/maven-proxy/") { name = "maven-proxy" }
    }

    dependencies {
        // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-dependencies
        implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.3")) {
            because("Spring Boot BOM")
        }

        // dependencies for all projects
        //        developmentOnly("org.springframework.boot:spring-boot-devtools")
        compileOnly("dev.slne.surf:surf-api-core-api:1.21+")
        compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")

        // Annotation processors
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:3.3.3")
        annotationProcessor("com.google.auto.service:auto-service:1.1.1")

        // Tests
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks {
        withType<ShadowJar> {
            mergeServiceFiles()
        }
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    publishing {
        repositories {
            maven("https://repo.slne.dev/repository/maven-snapshots/") { name = "maven-snapshots" }
        }

        publications.create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
