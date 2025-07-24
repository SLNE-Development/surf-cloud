import dev.slne.surf.surfapi.gradle.util.slneReleases
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xwhen-guards"))
}