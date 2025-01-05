plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
    `core-convention`
}

dependencies {
    api(project(":surf-cloud-test-plugin:surf-cloud-test-plugin-core"))
}

surfPaperPluginApi {
    paper {
        mainClass("com.example.Main")
    }
}