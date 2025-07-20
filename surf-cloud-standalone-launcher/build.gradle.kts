import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    `exclude-kotlin`
    id("dev.slne.surf.surfapi.gradle.standalone")

    application
    alias(libs.plugins.spring.boot)
}

surfStandaloneApi {
    addSurfApiToClasspath(false)
}

dependencies {
    implementation(libs.bundles.maven.libraries)
    implementation(libs.spring.instrument)
}

application {
    mainClass.set("dev.slne.surf.cloud.launcher.Main")
}

tasks {
    val standaloneProject = project(":surf-cloud-standalone")
    val standaloneJarTask = standaloneProject.tasks.named<BootJar>("bootJar")

    val copyStandaloneJar by registering(Copy::class) {
        dependsOn(standaloneJarTask)

        from(standaloneJarTask.flatMap { it.archiveFile })
        into(layout.buildDirectory.dir("libs"))
        rename { "surf-cloud-standalone.jara" }

        inputs.file(standaloneJarTask.flatMap { it.archiveFile })
        outputs.file(layout.buildDirectory.file("libs/surf-cloud-standalone.jara"))

        doFirst {
            val standaloneJar = standaloneJarTask.get().archiveFile.get().asFile

            if (!standaloneJar.exists()) {
                throw GradleException("Standalone JAR file does not exist: ${standaloneJar.absolutePath}")
            }
        }
    }
    val cleanupCopyStandaloneJar by registering(Delete::class) {
        delete(layout.buildDirectory.dir("libs").map { it.file("surf-cloud-standalone.jara") })
    }

    bootJar {
        dependsOn(copyStandaloneJar)
        dependsOn(standaloneJarTask)

        from(layout.buildDirectory.file("libs/surf-cloud-standalone.jara"))
        from(resources.text.fromString("org.springframework.boot.loader.launch.JarLauncher")) {
            into("META-INF")
            rename { "main-class" }
        }

        from("$buildDir/classes/java/main/dev/slne/surf/cloud/launcher/LauncherAgent.class") {
            into("dev/slne/surf/cloud/launcher")
        }

        val springInstrumentJar = configurations.runtimeClasspath.get()
            .find { it.name.contains("spring-instrument") } ?: throw GradleException("spring-instrument.jar not found")

        from(zipTree(springInstrumentJar)) {
            include("org/springframework/instrument/InstrumentationSavingAgent.class")
            into("")
        }

        manifest {
            attributes(
                "Launcher-Agent-Class" to "dev.slne.surf.cloud.launcher.LauncherAgent"
            )
        }

        doLast {
            file("${buildDirectory}/libs/surf-cloud-standalone.jara").delete()
        }

        finalizedBy(cleanupCopyStandaloneJar)
    }

    publish {
        dependsOn(bootJar)
        inputs.files(bootJar)
    }
}

private val Project.buildDirectory: File
    get() = layout.buildDirectory.asFile.get()