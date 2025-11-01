package dev.slne.surf.cloudtest.standalone.test

import dev.slne.surf.cloud.api.server.plugin.bootstrap.BootstrapContext
import dev.slne.surf.cloud.api.server.plugin.bootstrap.StandalonePluginBootstrap
import dev.slne.surf.cloudtest.TestSpringApplication

class TestStandaloneBootstrap : StandalonePluginBootstrap {
    override suspend fun bootstrap(context: BootstrapContext) {
        TestSpringApplication.run()
    }
}