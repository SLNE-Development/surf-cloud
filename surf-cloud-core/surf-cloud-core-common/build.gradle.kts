plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(project(":surf-cloud-api:surf-cloud-api-common")) {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }

    compileOnlyApi(libs.velocity.native)
    api(libs.zstd.jni)
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.cloud.api.common.util.annotation.InternalApi")
    }
}