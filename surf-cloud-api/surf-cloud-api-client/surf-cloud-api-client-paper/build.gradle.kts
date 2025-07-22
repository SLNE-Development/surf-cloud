import dev.slne.surf.surfapi.gradle.util.slneReleases

plugins {
    `exclude-kotlin`
    id("dev.slne.surf.surfapi.gradle.paper-raw")
}

dependencies {
    api(project(":surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-common"))
}

publishing {
    repositories {
        slneReleases()
    }
}