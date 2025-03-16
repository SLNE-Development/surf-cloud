plugins {
    id("dev.slne.surf.surfapi.gradle.standalone")
    alias(libs.plugins.spring.boot)
}

dependencies {
    api(project(":surf-cloud-core:surf-cloud-core-common"))
    api(project(":surf-cloud-api:surf-cloud-api-server"))

    runtimeOnly(libs.mariadb.java.client)
    api(libs.spring.boot.starter.data.jpa)
    api(libs.reactive.streams)
    api(libs.velocity.native)

    implementation(libs.hibernate.jcache)
    implementation(libs.ehcache)


    // Ktor
    implementation(libs.ktor.server.status.pages)

    implementation(libs.spring.boot.starter.log4j2)
    modules {
        module("org.springframework.boot:spring-boot-starter-logging") {
            replacedBy(
                libs.spring.boot.starter.log4j2.get().toString(),
                "Use Log4j2 instead of Logback"
            )
        }
    }
}

tasks {
    publish {
        dependsOn(bootJar)
        inputs.files(bootJar)
    }
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.cloud.api.common.util.annotation.InternalApi")
    }
}