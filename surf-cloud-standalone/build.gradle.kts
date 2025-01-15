plugins {
    id("dev.slne.surf.surfapi.gradle.standalone")
    `core-convention`
    `library-conventions`
}

dependencies {
    api(project(":surf-cloud-core:surf-cloud-core-common"))
    api(project(":surf-cloud-api:surf-cloud-api-server"))

    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    libraryApi("org.springframework.boot:spring-boot-starter-data-jpa")
    libraryApi("org.springframework.boot:spring-boot-starter-data-redis")
    libraryApi("org.reactivestreams:reactive-streams:1.0.4")
    libraryApi("io.lettuce:lettuce-core")
    libraryApi(libs.velocity.native)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "dev.slne.surf.cloud.standalone.launcher.Launcher"
            attributes["Real-Main-Class"] = "dev.slne.surf.cloud.standalone.Bootstrap"
        }
    }
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.cloud.api.common.util.InternalApi")
    }
}


//tasks.register("generateDependenciesFile") {
//    group = "reporting"
//    description = "Generates a file listing all dependencies in the library and libraryApi scopes."
//
//    val outputFile = layout.buildDirectory.file("dependencies")
//
//    inputs.files(library, libraryApi) // track input changes for up-to-date checks
//    outputs.file(outputFile)
//
//    doLast {
//        val dependenciesFile = outputFile.get().asFile
//        dependenciesFile.bufferedWriter().use { writer ->
//            // Library dependencies
//            library.resolve().forEach { dependency ->
//                writer.write(dependency.toString())
//                writer.newLine()
//            }
//
//            // LibraryApi dependencies
//            libraryApi.resolve().forEach { dependency ->
//                writer.write(dependency.toString())
//                writer.newLine()
//            }
//        }
//        logger.lifecycle("Dependencies file generated at: ${dependenciesFile.absolutePath}")
//    }
//}
//
//tasks.register("generateReposFile") {
//    group = "reporting"
//    description = "Generates a file listing all repository URLs."
//
//    val outputFile = layout.buildDirectory.file("repos")
//
//    outputs.file(outputFile)
//
//    doLast {
//        val reposFile = outputFile.get().asFile
//        reposFile.bufferedWriter().use { writer ->
//            repositories.forEach { repo ->
//                when (repo) {
//                    is MavenArtifactRepository -> {
//                        writer.write(repo.url.toString())
//                        writer.newLine()
//                    }
//                }
//            }
//        }
//        logger.lifecycle("Repos file generated at: ${reposFile.absolutePath}")
//    }
//}
//
//tasks.register("generateAll") {
//    group = "reporting"
//    description = "Generates both dependencies and repos files."
//
//    dependsOn("generateDependenciesFile", "generateReposFile")
//}

tasks.shadowJar {
    dependsOn("generateAll")
}