plugins {
    `exclude-kotlin`
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(project(":surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-common"))
    api(project(":surf-cloud-core:surf-cloud-core-common"))

    compileOnly(libs.luckperms.api)
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.cloud.api.common.util.annotation.InternalApi")
    }
}