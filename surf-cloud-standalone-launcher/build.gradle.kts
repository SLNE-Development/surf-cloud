import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("dev.slne.surf.surfapi.gradle.standalone")

    application
    `core-convention`
}

surfStandaloneApi {
    addSurfApiToClasspath(false)
    shadeKotlin(false)
}

application {
    mainClass.set("dev.slne.surf.cloud.launcher.Main")
}

tasks {
    val standaloneProject = project(":surf-cloud-standalone")

    val copyStandaloneJar by registering(Copy::class) {
        val standaloneJarTask = standaloneProject.tasks.named<ShadowJar>("shadowJar")
        dependsOn(standaloneJarTask)

        from(standaloneJarTask.map { it.archiveFile.get().asFile })
        into(buildDirectory.resolve("libs"))
        rename { "surf-cloud-standalone.jara" }

        doFirst {
            val standaloneJar = standaloneJarTask.get().archiveFile.get().asFile

            if (!standaloneJar.exists()) {
                throw GradleException("Standalone JAR file does not exist: ${standaloneJar.absolutePath}")
            }
        }
    }

    shadowJar {
        dependsOn(copyStandaloneJar)
        from(buildDirectory.resolve("libs/surf-cloud-standalone.jara"))

        manifest {
            attributes(
                "Standalone-Main-Class" to "dev.slne.surf.cloud.standalone.Bootstrap"
            )
        }

        doLast {
            file("${buildDirectory}/libs/surf-cloud-standalone.jara").delete()
        }
    }

    named<JavaExec>("run") {
        dependsOn(shadowJar)
    }
}

private val Project.buildDirectory: File
    get() = layout.buildDirectory.asFile.get()