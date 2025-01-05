import dev.slne.surf.surfapi.gradle.util.registerRequired

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
    `core-convention`
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.cloud.bukkit.BukkitMain")
    bootstrapper("dev.slne.surf.cloud.bukkit.BukkitBootstrap")
    authors.add("twisti")

    serverDependencies {
        registerRequired("LuckPerms")
    }

    runServer {
        jvmArgs("-Dsurf.cloud.serverName=test-server01")
    }
}


dependencies {
    api(project(":surf-cloud-core:surf-cloud-core-client"))

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
    runtimeClasspath {
        exclude(group = "org.reactivestreams", module = "reactive-streams")
    }
}