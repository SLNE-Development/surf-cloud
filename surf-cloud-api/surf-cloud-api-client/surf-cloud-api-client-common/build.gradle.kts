import dev.slne.surf.surfapi.gradle.util.slneReleases

plugins {
    `exclude-kotlin`
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(project(":surf-cloud-api:surf-cloud-api-common"))
}

publishing {
    repositories {
        slneReleases()
    }
}