plugins {
    java
    `java-library`
    `maven-publish`
    id("io.spring.dependency-management") version "1.1.6"
    id("org.hibernate.build.maven-repo-auth") version "3.0.4"
    id("io.github.goooler.shadow") version "8.1.8"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

allprojects {
    group = "dev.slne.surf.data"
    version = "1.21.1-1.0.0-SNAPSHOT"

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
        maven("https://repo.slne.dev/repository/maven-snapshots/") { name = "maven-snapshots" }
        maven("https://repo.slne.dev/repository/maven-proxy/") { name = "maven-proxy" }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.gradle.java-library")
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.hibernate.build.maven-repo-auth")
    apply(plugin = "io.github.goooler.shadow")

    dependencies {
        compileOnly("org.projectlombok:lombok")
//        developmentOnly("org.springframework.boot:spring-boot-devtools")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        compileOnly("dev.slne.surf:surf-api-core-api:1.21+")

        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        annotationProcessor("org.projectlombok:lombok")
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.3")
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


