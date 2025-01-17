//val springBootBom = "org.springframework.boot:spring-boot-dependencies:3.3.3"
//project.dependencies.apply {
//    add("implementation", platform(springBootBom))
//}
//
//project.configurations.configureEach {
//    if (isCanBeResolved && isCanBeConsumed) {
//        dependencies.add(project.dependencies.platform(springBootBom))
//    }
//}
//
//val library: Configuration =
//    configurations.maybeCreate("library").extendsFrom(configurations.getByName("compileOnly"))
//val libraryApi: Configuration =
//    configurations.maybeCreate("libraryApi").extendsFrom(configurations.getByName("compileOnlyApi"))
//
//tasks.register("generateAll") {
//    group = "reporting"
//    description = "Generates both dependencies and repos files."
//
//    val librariesRootComponent = libraryApi.incoming.resolutionResult.root
//    val repos = project.repositories.withType(MavenArtifactRepository::class.java)
//    val dependencies = librariesRootComponent.collectLibraries()
//
//    repos.forEach { repo ->
//        println("Found Repo: ${repo.url}")
//    }
//
//    dependencies.forEach { dependency ->
//        println("Found Dependency: ${dependency}")
//    }
//}