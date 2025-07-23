import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.slne.surf.surfapi.gradle.util.slnePublic

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    }
    dependencies {
        classpath("dev.slne.surf:surf-api-gradle-plugin:1.21.7+")
    }
}

plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.17.0"
//    id("io.freefair.aspectj.post-compile-weaving") version "8.13.1"
    java
}

allprojects {
    group = "dev.slne.surf.cloud"
    version = findProperty("version") as String

    repositories {
        slnePublic()
    }

    if (name == "surf-cloud-bom") {
        return@allprojects
    }

    apply(plugin = "java")
//    apply(plugin = "io.freefair.aspectj.post-compile-weaving")

    dependencies {
//        implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.4"))
//        implementation(platform("io.ktor:ktor-bom:3.0.3"))
//        implementation(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:2025.4.10"))

        implementation(platform(project(":surf-cloud-bom")))

        compileOnly("org.springframework.boot:spring-boot-configuration-processor:3.5.3")
        //    "kapt"("org.springframework.boot:spring-boot-configuration-processor:3.4.3")

        testImplementation(kotlin("test"))
    }


    tasks {
        configureShadowJar()
        configureJar()

        javadoc {
            val options = options as StandardJavadocDocletOptions
            options.use()
            options.tags("implNote:a:Implementation Note:")
        }

        test {
            useJUnitPlatform()
        }
    }
}

apiValidation {
    ignoredProjects.addAll(
        listOf(
            "surf-cloud-core",
            "surf-cloud-core-common",
            "surf-cloud-core-client",
            "surf-cloud-bukkit",
            "surf-cloud-velocity",
            "surf-cloud-standalone",
            "surf-cloud-standalone-launcher",
            "surf-cloud-test-standalone"
        )
    )
    nonPublicMarkers.add("dev.slne.surf.cloud.api.common.util.annotation.InternalApi")
}

private fun TaskContainerScope.configureShadowJar() = withType<ShadowJar> {
    mergeServiceFiles {
        path = "META-INF"
        exclude("META-INF/MANIFEST.MF")
    }

    isZip64 = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

private fun TaskContainerScope.configureJar() = withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}