plugins {
    `exclude-kotlin`
    id("dev.slne.surf.surfapi.gradle.velocity")
}

dependencies {
    api(project(":surf-cloud-core:surf-cloud-core-client"))
    api(project(":surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-velocity"))
}

velocityPluginFile {
    main = "dev.slne.surf.cloud.velocity.VelocityMain"
}

configurations {
    all {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    runtimeClasspath {
        exclude(group = "org.reactivestreams", module = "reactive-streams")
    }
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.cloud.api.common.util.annotation.InternalApi")
    }
}