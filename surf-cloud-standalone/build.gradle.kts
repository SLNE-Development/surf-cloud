plugins {
    id("dev.slne.surf.surfapi.gradle.standalone")
    `core-convention`
    id("org.springframework.boot")
}

dependencies {
    api(project(":surf-cloud-core:surf-cloud-core-common"))
    api(project(":surf-cloud-api:surf-cloud-api-server"))

    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.reactivestreams:reactive-streams:1.0.4")
    api(libs.velocity.native)

    implementation("org.hibernate.orm:hibernate-jcache")
    implementation("org.ehcache:ehcache:3.10.8")


    // Ktor
    implementation("io.ktor:ktor-server-status-pages")

    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    modules {
        module("org.springframework.boot:spring-boot-starter-logging") {
            replacedBy(
                "org.springframework.boot:spring-boot-starter-log4j2",
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