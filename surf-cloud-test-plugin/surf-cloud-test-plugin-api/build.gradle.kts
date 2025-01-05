plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    `core-convention`
}

dependencies {
    compileOnlyApi(project(":surf-cloud-api:surf-cloud-api-common"))
    compileOnlyApi("org.springframework.boot:spring-boot-starter-data-jpa")
}