import java.util.*

plugins {
    id("dev.slne.surf.surfapi.gradle.standalone")
    alias(libs.plugins.spring.boot)
}

repositories {
    maven("https://jitpack.io")
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
    implementation(fileTree("libs") {
        include("*.jar")
    })
}

tasks {
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