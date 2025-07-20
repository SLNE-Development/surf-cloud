import dev.slne.surf.surfapi.gradle.util.slneReleases

plugins {
    `exclude-kotlin`
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(project(":surf-cloud-api:surf-cloud-api-common"))

    api(libs.bundles.flyway)
    api(libs.bundles.exposed.api.server)
    api(libs.bundles.maven.libraries)
    api(libs.bundles.console.api)
    api(libs.bundles.ktor.api.server)
    api(libs.bson.kotlinx)
    api(libs.spring.boot.starter.actuator)
//    api(libs.discord.webhooks) { isTransitive = true }
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.cloud.api.common.util.annotation.InternalApi")
    }
}

publishing {
    repositories {
        slneReleases()
    }
}