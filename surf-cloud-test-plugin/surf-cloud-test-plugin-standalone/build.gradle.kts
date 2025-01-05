plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    `core-convention`
}

dependencies {
    compileOnlyApi(project(":surf-cloud-standalone"))
    api(project(":surf-cloud-test-plugin:surf-cloud-test-plugin-core"))
}

tasks.withType<Jar> {
    val manifest = mapOf(
        "Plugin-Id" to "test-standalone-plugin",
        "Plugin-Class" to "dev.slne.surf.cloud.test.standalone.TestStandalonePlugin",
        "Plugin-Version" to "1.0.0-STANDALONE",
        "Plugin-Provider" to "SLNE",
        "Plugin-Dependencies" to ""
    )

    manifest {
        attributes(manifest)
    }
}