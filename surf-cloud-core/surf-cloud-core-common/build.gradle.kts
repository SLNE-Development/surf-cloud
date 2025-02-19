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

    // https://mvnrepository.com/artifact/com.github.luben/zstd-jni
    api("com.github.luben:zstd-jni:1.5.6-9")
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.cloud.api.common.util.annotation.InternalApi")
    }
}