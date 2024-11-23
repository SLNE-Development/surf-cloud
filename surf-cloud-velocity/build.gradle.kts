plugins {
    `core-convention`
}

dependencies {
    api(project(":surf-cloud-core:surf-cloud-core-client"))
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
//    kapt("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.springframework.boot:spring-boot-starter-jooq")
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.core:jackson-databind")
}