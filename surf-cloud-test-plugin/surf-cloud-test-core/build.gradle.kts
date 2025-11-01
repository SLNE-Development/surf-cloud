plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    compileOnly(project(":surf-cloud-api:surf-cloud-api-common"))
}