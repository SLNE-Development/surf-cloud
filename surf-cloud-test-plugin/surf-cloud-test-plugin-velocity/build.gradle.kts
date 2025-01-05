plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
    `core-convention`
}

dependencies {
    api(project(":surf-cloud-test-plugin:surf-cloud-test-plugin-core"))
}