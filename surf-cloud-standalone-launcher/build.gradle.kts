plugins {
    application
}

application {
    mainClass.set("dev.slne.surf.cloud.launcher.Main")
}

tasks {
    shadowJar {
        dependsOn(":surf-cloud-standalone:shadowJar")
        val standalone = project(":surf-cloud-standalone").tasks.named("shadowJar")
            .map { it.outputs.files.singleFile }
            .get()

        // rename the standalone jar file
        val standaloneFile = File("surf-cloud-standalone.jara")
        standalone.renameTo(standaloneFile)

        from(standaloneFile)

        manifest {
            attributes(
                "Standalone-Main-Class" to "dev.slne.surf.cloud.standalone.Bootstrap"
            )
        }

        doLast { standaloneFile.delete() }
    }

    named<JavaExec>("run") {
        dependsOn(shadowJar)
    }
}