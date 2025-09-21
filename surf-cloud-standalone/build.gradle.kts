import dev.slne.surf.surfapi.gradle.util.slneReleases
import java.util.*
import java.util.zip.ZipFile

plugins {
    id("dev.slne.surf.surfapi.gradle.standalone")
    alias(libs.plugins.spring.boot)
}

repositories {
    maven("https://jitpack.io")
}

val sanitizedLibsDir = layout.buildDirectory.dir("sanitized-libs")
val sanitizeLibs by tasks.registering {
    inputs.files(fileTree("libs") { include("*.jar") })
    outputs.dir(sanitizedLibsDir)

    doLast {
        val outDir = sanitizedLibsDir.get().asFile
        outDir.mkdirs()

        fileTree("libs").matching { include("*.jar") }.files.forEach { inJar ->
            val hasProvider = ZipFile(inJar).use { zf ->
                zf.getEntry("META-INF/services/org.slf4j.spi.SLF4JServiceProvider") != null ||
                        zf.getEntry("org/slf4j/simple/SimpleServiceProvider.class") != null
            }

            val dest = outDir.resolve(inJar.name)
            if (!hasProvider) {
                inJar.copyTo(dest, overwrite = true)
            } else {
                val tmp = layout.buildDirectory.dir("tmp/sanitize/${inJar.nameWithoutExtension}").get().asFile
                tmp.deleteRecursively(); tmp.mkdirs()

                copy {
                    from(zipTree(inJar))
                    exclude(
                        "META-INF/services/org.slf4j.spi.SLF4JServiceProvider",
                        "org/slf4j/simple/**"
                    )
                    into(tmp)
                }
                ant.invokeMethod("zip", mapOf("basedir" to tmp.absolutePath, "destfile" to dest.absolutePath))
            }
        }
    }
}


dependencies {
    api(project(":surf-cloud-core:surf-cloud-core-common"))
    api(project(":surf-cloud-api:surf-cloud-api-server"))

    runtimeOnly(libs.mariadb.java.client)
    runtimeOnly(libs.mysql.connector.j)
    api(libs.reactive.streams)
    api(libs.velocity.native)

    // Ktor
    implementation(libs.ktor.server.status.pages)

    implementation(libs.spring.boot.starter.log4j2)
    modules {
        module("org.springframework.boot:spring-boot-starter-logging") {
            replacedBy(
                libs.spring.boot.starter.log4j2.get().toString(),
                "Use Log4j2 instead of Logback"
            )
        }
    }

//    implementation(fileTree("libs/**/*.jar")) // Include all JARs in libs directory
//    implementation(fileTree(sanitizedLibsDir) { include("*.jar") })
    implementation(fileTree(sanitizedLibsDir) { include("*.jar") }.builtBy(sanitizeLibs))
}

tasks {
//    assemble {
//        dependsOn(sanitizeLibs)
//        inputs.files(sanitizeLibs)
//    }
//
//    build {
//        dependsOn(sanitizeLibs)
//    }

    bootJar {
        mainClass.set("dev.slne.surf.cloud.standalone.Bootstrap")
    }
    publish {
        dependsOn(bootJar)
        inputs.files(bootJar)
    }

    register<JavaExec>("generateExposedMigrationScript") {
        group = "migration"
        description = "Generate Exposed migration script"
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass.set("dev.slne.surf.cloud.standalone.GenerateExposedMigrationScriptKt")

        val propertiesFile = file("migration.properties")

        doFirst {
            if (!propertiesFile.exists()) {
                propertiesFile.parentFile.mkdirs()
                propertiesFile.writeText(
                    """
                # Migration database config
                migration.dbUrl=jdbc:mysql://localhost:3306/database
                migration.dbUser=
                migration.dbPassword=
                """.trimIndent()
                )
                throw GradleException("Created 'migration.properties' file. Please enter your credentials and run the task again.")
            }

            val migrationProperties = Properties().apply {
                load(propertiesFile.inputStream())
            }

            val requiredKeys = listOf("migration.dbUrl", "migration.dbUser", "migration.dbPassword")
            val missing =
                requiredKeys.filter { migrationProperties.getProperty(it).isNullOrBlank() }
            if (missing.isNotEmpty()) {
                throw GradleException("'migration.properties' is incomplete. Missing keys: ${missing.joinToString()}")
            }

            systemProperties(
                requiredKeys.associateWith { migrationProperties.getProperty(it) }
            )
        }
    }
}


kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.cloud.api.common.util.annotation.InternalApi")
    }
}

publishing {
    repositories {
        slneReleases()
    }
}