import dev.slne.surf.surfapi.gradle.util.slneReleases

plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.3"))
    api(platform("io.ktor:ktor-bom:3.2.1"))
    api(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:2025.7.8"))
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