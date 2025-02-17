plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    `core-convention`
}

dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    // https://mvnrepository.com/artifact/tech.hiddenproject/aide-reflection
    api("tech.hiddenproject:aide-reflection:1.3")
    // https://mvnrepository.com/artifact/io.netty/netty-all
    api("io.netty:netty-all:4.1.117.Final")
    api("com.github.Querz:NBT:6.1")

    // https://mvnrepository.com/artifact/org.springframework/spring-jdbc
//    api("org.springframework:spring-jdbc")

    // https://mvnrepository.com/artifact/org.springframework.data/spring-data-jpa
    implementation("org.springframework.data:spring-data-jpa")

    api("com.mojang:datafixerupper:8.0.16") {
        isTransitive = false
    }
    api("net.bytebuddy:byte-buddy:1.15.10")
}