plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    `core-convention`
}

dependencies {
    api(project(":surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-common"))
}