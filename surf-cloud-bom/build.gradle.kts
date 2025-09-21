import dev.slne.surf.surfapi.gradle.util.slneReleases

plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.6"))
    api(platform("io.ktor:ktor-bom:3.3.0"))
    api(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:2025.9.8"))
}

configurations.all {
    exclude(group = "ch.qos.logback", module = "logback-classic")
    exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j2-impl")
}

publishing {
    publications {
        create<MavenPublication>("mavenBom") {
            from(components["javaPlatform"])
        }
    }

    repositories {
        slneReleases()
    }
}