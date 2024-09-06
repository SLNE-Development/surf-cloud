import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    java
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

dependencies {
    api(project(":surf-data-core"))
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    paperLibrary("org.springframework.boot:spring-boot-starter-data-jpa")
    paperLibrary("org.springframework.boot:spring-boot-starter-data-redis")
    paperLibrary("org.springframework.boot:spring-boot-starter-jooq")
    paperLibrary("org.springframework.boot:spring-boot-starter-validation")
    paperLibrary("org.flywaydb:flyway-core")
    paperLibrary("org.flywaydb:flyway-mysql")
}

tasks {
    runServer {
        minecraftVersion("1.21.1")
    }
}

paper {
    main = "dev.slne.surf.data.bukkit.BukkitMain"
    apiVersion = "1.21"
    authors = listOf("twisti", "SLNE Development")

    generateLibrariesJson = true

    serverDependencies {
        register("surf-bukkit-api") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
        }
    }
}