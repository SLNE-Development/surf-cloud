plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(libs.bundles.spring.api.common)
    implementation(libs.spring.data.jpa) // Hide this from the API
    api(libs.bundles.jackson.api.common)

    api(libs.aide.reflection)
    api(libs.netty.all)
    api(libs.nbt)
    api(libs.datafixerupper) {
        isTransitive = false
    }
    api(libs.byte.buddy)
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.cloud.api.common.util.annotation.InternalApi")
    }
}