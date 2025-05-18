import dev.slne.surf.surfapi.gradle.util.slneReleases

plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.4.4"))
    api(platform("io.ktor:ktor-bom:3.0.3"))
    api(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:2025.4.10"))
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