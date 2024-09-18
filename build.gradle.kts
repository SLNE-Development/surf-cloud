plugins {
    java
    `java-library`
    `maven-publish`
//    id("io.spring.dependency-management") version "1.1.6"
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
    apply(plugin = "java")
    apply(plugin = "org.gradle.java-library")
    apply(plugin = "io.github.goooler.shadow")
    apply(plugin = "org.gradle.maven-publish")
//    apply(plugin = "io.spring.dependency-management")

    group = "dev.slne.surf.cloud"
    version = "1.21.1-1.0.0-SNAPSHOT"

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
        maven("https://repo.slne.dev/repository/maven-snapshots/") { name = "maven-snapshots" }
        maven("https://repo.slne.dev/repository/maven-proxy/") { name = "maven-proxy" }
    }

    tasks {
        shadowJar {
            mergeServiceFiles()
        }
    }

    dependencies {
        // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-dependencies
        implementation("org.springframework.boot:spring-boot-dependencies:3.3.3") {
            because("Spring Boot dependencies")
        }
    }

//    the<DependencyManagementExtension>().apply {
//        imports {
//            mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.3")
//            // https://mvnrepository.com/artifact/org.springframework/spring-framework-bom
//            mavenBom("org.springframework:spring-framework-bom:6.1.13")
//
//        }
//    }
}
subprojects {
    apply(plugin = "org.gradle.java-library")
    apply(plugin = "org.gradle.maven-publish")
//    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.hibernate.build.maven-repo-auth")

    dependencies {

//        developmentOnly("org.springframework.boot:spring-boot-devtools")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        compileOnly("dev.slne.surf:surf-api-core-api:1.21+")

        // Annotation processors
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
        annotationProcessor("com.google.auto.service:auto-service:1.1.1")
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
