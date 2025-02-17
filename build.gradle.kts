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
        classpath("dev.slne.surf:surf-api-gradle-plugin:1.21.4+")
    }
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