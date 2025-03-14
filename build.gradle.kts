import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.slne.surf.surfapi.gradle.util.slnePublic
import org.jetbrains.kotlin.noarg.gradle.NoArgExtension
import org.jetbrains.kotlin.noarg.gradle.NoArgGradleSubplugin

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    }
    dependencies {
        classpath("dev.slne.surf:surf-api-gradle-plugin:1.21.4-1.0.121")
    }
}

plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.17.0"
}

allprojects {
    tasks {
        withType<ShadowJar> {
            mergeServiceFiles {
                setPath("META-INF")
                exclude("META-INF/MANIFEST.MF")
            }

            isZip64 = true
        }

        withType<Jar> {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }

    afterEvaluate {
        if (plugins.hasPlugin(PublishingPlugin::class)) {
            configure<PublishingExtension> {
                repositories {
                    slnePublic()
                }
            }
        }

        if (plugins.hasPlugin(NoArgGradleSubplugin::class)) {
            configure<NoArgExtension> {
                annotation("jakarta.persistence.Entity")
                annotation("jakarta.persistence.MappedSuperclass")
                annotation("jakarta.persistence.Embeddable")
                invokeInitializers = true
            }
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