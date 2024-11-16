import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    `java-library`
    `maven-publish`

    id("org.hibernate.build.maven-repo-auth") version "3.0.4"
    id("com.gradleup.shadow") version "8.3.3"

    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.lombok") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
    kotlin("kapt") version "2.0.21"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

allprojects {
    apply(plugin = "org.gradle.java-library")
    apply(plugin = "com.gradleup.shadow")

    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.hibernate.build.maven-repo-auth")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.lombok")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.kapt")

    group = "dev.slne.surf.cloud"
    version = "1.21.1-1.0.0-SNAPSHOT"

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
        maven("https://repo.slne.dev/repository/maven-snapshots/") { name = "maven-snapshots" }
        maven("https://repo.slne.dev/repository/maven-proxy/") { name = "maven-proxy" }
        maven("https://repo.glaremasters.me/repository/public/")
    }

    dependencies {
        implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.5")) {
            because("Spring Boot BOM")
        }
        // dsl
        val autoDslVersion = "2.4.0"
        implementation("com.faendir.kotlin.autodsl:annotations:$autoDslVersion")
        kapt("com.faendir.kotlin.autodsl:processor:$autoDslVersion")

        // dependencies for all projects
        //        developmentOnly("org.springframework.boot:spring-boot-devtools")
        compileOnly("dev.slne.surf:surf-api-core-api:1.21+")
        compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
        api(kotlin("reflect"))
        // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-reactive
        api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")

//    implementation("org.springframework.boot:spring-boot-configuration-processor:3.3.3")
        kapt("org.springframework.boot:spring-boot-configuration-processor:3.3.3")
//    implementation("com.google.auto.service:auto-service:1.1.1")
        kapt("com.google.auto.service:auto-service:1.1.1")
    }


    tasks {
        withType<ShadowJar> {
            mergeServiceFiles {
                setPath("META-INF")
                exclude("META-INF/MANIFEST.MF")
            }
        }

        jar {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
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

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
//        extraWarnings.set(true)
    }
}
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
