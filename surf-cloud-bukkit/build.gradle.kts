import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    java
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

dependencies {
    api(project(":surf-cloud-core"))
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    api("org.springframework.boot:spring-boot-starter-cloud-jpa")
    api("org.springframework.boot:spring-boot-starter-cloud-redis")
    api("org.springframework.boot:spring-boot-starter-jooq")
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.core:jackson-databind")
}

tasks {
    runServer {
        minecraftVersion("1.21.1")
    }
}

paper {
    main = "dev.slne.surf.cloud.bukkit.BukkitMain"
    loader = "dev.slne.surf.cloud.bukkit.BukkitLoader"
    apiVersion = "1.21"
    authors = listOf("twisti", "SLNE Development")

//    generateLibrariesJson = true

    serverDependencies {
        register("surf-bukkit-api") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
        }
    }
}