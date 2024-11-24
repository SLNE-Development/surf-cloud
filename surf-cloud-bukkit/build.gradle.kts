import net.minecrell.pluginyml.paper.PaperPluginDescription


plugins {
    `core-convention`
    java
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("io.papermc.paperweight.userdev") version "1.7.5"
}

dependencies {
    api(project(":surf-cloud-core:surf-cloud-core-client"))
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    compileOnly("dev.slne.surf:surf-api-bukkit-api:1.21+")

    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.springframework.boot:spring-boot-starter-jooq")
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.core:jackson-databind")
}

configurations {
    all {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
}

tasks {
    runServer {
        minecraftVersion("1.21.1")
        jvmArgs("-Dsurf.cloud.serverName=test")
    }
    assemble {
        dependsOn(reobfJar)
    }
}

paper {
    main = "dev.slne.surf.cloud.bukkit.BukkitMain"
    loader = "dev.slne.surf.cloud.bukkit.BukkitLoader"
    apiVersion = "1.21"
    authors = listOf("twisti", "SLNE Development")

//    generateLibrariesJson = true

    bootstrapDependencies {
        register("surf-bukkit-api") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
        }
    }

    serverDependencies {
        register("surf-bukkit-api") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
        }
    }
}