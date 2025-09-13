import dev.slne.surf.surfapi.gradle.util.slneReleases
import gradle.kotlin.dsl.accessors._2c95f20277cbe6143532f6e8d67e36cc.processResources
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.jetbrains.kotlin.gradle.idea.proto.com.google.protobuf.api
import org.jetbrains.kotlin.gradle.internal.config.AnalysisFlags.optIn

plugins {
    `exclude-kotlin`
    id("dev.slne.surf.surfapi.gradle.core")
    `generate-stream-codec`
}

dependencies {
    api(libs.bundles.spring.api.common)
    api(libs.bundles.jackson.api.common)
    api(libs.bundles.spring.aop)

    api(libs.aide.reflection)
    api(libs.bundles.netty.all)
    api(libs.kotlin.byte.buf.serializer) {
        exclude(group = "io.netty")
    }

    api(libs.datafixerupper) {
        isTransitive = false
    }
    api(libs.byte.buddy)
}

val writeCloudVersion by tasks.registering {
    group = "build"
    description = "Writes the cloud version to the classpath resource"

    val outputDir = layout.projectDirectory.dir("src/main/resources")
    val outputFile = outputDir.file("cloud.version")

    doLast {
        outputDir.asFile.mkdirs()
        outputFile.asFile.writeText(project.version as String)
    }
}

tasks {
    processResources {
        dependsOn(writeCloudVersion)
    }
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