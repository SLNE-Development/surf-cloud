buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.slne.dev/repository/maven-unsafe/") { name = "maven-unsafe" }
    }
    dependencies {
        classpath("dev.slne.surf:surf-api-gradle-plugin:1.21.4-1.0.44-SNAPSHOT")
    }
}
