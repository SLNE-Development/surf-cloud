plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    compileOnly(project(":surf-cloud-api:surf-cloud-api-server"))

    compileOnly("commons-io:commons-io:2.19.0")
}