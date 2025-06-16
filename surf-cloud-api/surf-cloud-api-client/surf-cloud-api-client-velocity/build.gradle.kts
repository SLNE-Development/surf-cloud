import dev.slne.surf.surfapi.gradle.util.slneReleases

plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
}

dependencies {
    api(project(":surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-common"))
}

publishing {
    repositories {
        slneReleases()
    }
}