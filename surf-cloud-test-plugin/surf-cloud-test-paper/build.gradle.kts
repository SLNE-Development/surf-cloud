import dev.slne.surf.surfapi.gradle.util.registerRequired

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

dependencies {
    api(project(":surf-cloud-test-plugin:surf-cloud-test-core"))
    compileOnly(project(":surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-paper"))
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.cloudtest.paper.PaperMain")
    bootstrapper("dev.slne.surf.cloudtest.paper.PaperBootstrapper")

    bootstrapDependencies {
        registerRequired("surf-cloud-bukkit")
    }

    serverDependencies {
        registerRequired("surf-cloud-bukkit")
    }
}