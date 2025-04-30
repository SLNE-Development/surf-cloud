import dev.slne.surf.surfapi.gradle.util.registerRequired
import dev.slne.surf.surfapi.gradle.util.registerSoft

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
    `exclude-kotlin`
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.cloud.bukkit.BukkitMain")
    bootstrapper("dev.slne.surf.cloud.bukkit.BukkitBootstrap")
    authors.add("twisti")
    generateLibraryLoader(false)

    serverDependencies {
        registerRequired("LuckPerms")
        registerSoft("voicechat")
    }

    runServer {
        jvmArgs("-Dsurf.cloud.serverName=test-server01")
    }
}

repositories {
    maven("https://maven.maxhenkel.de/repository/public")
}

dependencies {
    api(project(":surf-cloud-core:surf-cloud-core-client"))
    api(project(":surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-paper"))

    compileOnly(libs.voicechat.api)

//    api("org.springframework.boot:spring-boot-starter-data-jpa")
}

configurations {
    all {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    runtimeClasspath {
        exclude(group = "org.reactivestreams", module = "reactive-streams")
    }
}