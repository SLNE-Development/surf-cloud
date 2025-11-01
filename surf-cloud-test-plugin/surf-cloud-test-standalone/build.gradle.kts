plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(project(":surf-cloud-test-plugin:surf-cloud-test-core"))
    compileOnly(project(":surf-cloud-api:surf-cloud-api-server"))

    // https://mvnrepository.com/artifact/commons-io/commons-io
    compileOnly("commons-io:commons-io:2.19.0")
}