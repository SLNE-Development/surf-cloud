plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    `core-convention`
}

dependencies {
    api(project(":surf-cloud-api:surf-cloud-api-common")) {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }

    runtimeOnly("org.aspectj:aspectjweaver:1.9.22.1")
    compileOnlyApi(libs.velocity.native)
}