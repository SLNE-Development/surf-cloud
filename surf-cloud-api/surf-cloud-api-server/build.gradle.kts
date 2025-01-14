plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    `core-convention`
}

dependencies {
    api(project(":surf-cloud-api:surf-cloud-api-common"))

    api("org.apache.maven:maven-impl:4.0.0-rc-2")
    api("org.apache.maven.resolver:maven-resolver-supplier-mvn4:2.0.5")
}