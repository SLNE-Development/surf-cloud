dependencies {
    api(project(":surf-cloud-core"))
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    api("org.springframework.boot:spring-boot-starter-cloud-jpa")
    api("org.springframework.boot:spring-boot-starter-cloud-redis")
    api("org.springframework.boot:spring-boot-starter-jooq")
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.core:jackson-databind")
}