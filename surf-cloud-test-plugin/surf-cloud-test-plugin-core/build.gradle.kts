plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    `core-convention`
}

dependencies {
    api(project(":surf-cloud-test-plugin:surf-cloud-test-plugin-api"))
    compileOnlyApi(project(":surf-cloud-api:surf-cloud-api-client"))
}