import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.slne.dev/repository/maven-unsafe/") { name = "maven-unsafe" }
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
}