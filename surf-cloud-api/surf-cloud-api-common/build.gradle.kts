plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(libs.bundles.spring.api.common)
    implementation(libs.spring.data.jpa) // Hide this from the API
    api(libs.bundles.jackson.api.common)
    api(libs.bundles.spring.aop)

    api(libs.aide.reflection)
    api(libs.bundles.netty.all)
    api(libs.kotlin.byte.buf.serializer) {
        exclude(group = "io.netty")
    }

    api(libs.nbt)
    api(libs.datafixerupper) {
        isTransitive = false
    }
    api(libs.byte.buddy)
    api(libs.spring.boot.starter.actuator)
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.cloud.api.common.util.annotation.InternalApi")
    }
}