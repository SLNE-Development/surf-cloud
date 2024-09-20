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

        val standaloneFile = File(standalone.parentFile, "surf-cloud-standalone.jara")
        standalone.renameTo(standaloneFile)

        from(standaloneFile)

        // add test txt file to the jar
        val testFile = File("te.txt")
        testFile.writeText("This is a test file")

        from(testFile)

        manifest {
            attributes(
                "Standalone-Main-Class" to "dev.slne.surf.cloud.standalone.Bootstrap"
            )
        }
    }
}