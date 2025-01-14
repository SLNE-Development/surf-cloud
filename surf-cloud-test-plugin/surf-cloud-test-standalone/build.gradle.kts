plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    `core-convention`
}

dependencies {
    compileOnly(project(":surf-cloud-api:surf-cloud-api-server"))
    // https://mvnrepository.com/artifact/commons-io/commons-io
    compileOnly("commons-io:commons-io:2.18.0")

}