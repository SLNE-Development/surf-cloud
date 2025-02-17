plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    `core-convention`
}

dependencies {
    api(project(":surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-common"))
    api(project(":surf-cloud-core:surf-cloud-core-common"))

    compileOnly("net.luckperms:api:5.4")
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.cloud.api.common.util.InternalApi")
    }
}