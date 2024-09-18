plugins {
}

dependencies {
    compileOnlyApi("org.springframework.boot:spring-boot-starter-cloud-jpa")
    compileOnlyApi("org.springframework.boot:spring-boot-starter-cloud-redis")
    compileOnlyApi("org.springframework.boot:spring-boot-starter-jooq")
    compileOnlyApi("com.fasterxml.jackson.core:jackson-core")
    compileOnlyApi("com.fasterxml.jackson.core:jackson-databind")
    // https://mvnrepository.com/artifact/tech.hiddenproject/aide-reflection
    api("tech.hiddenproject:aide-reflection:1.3")
    // https://mvnrepository.com/artifact/io.netty/netty-all
    api("io.netty:netty-all:4.1.113.Final")
    api("com.github.Querz:NBT:6.1")
}