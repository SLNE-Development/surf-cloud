package dev.slne.surf.cloud.standalone.test

import dev.slne.surf.cloud.api.common.cloudInstance
import dev.slne.surf.cloud.api.common.startSpringApplication
import dev.slne.surf.cloud.api.server.server.plugin.bootstrap.BootstrapContext
import dev.slne.surf.cloud.api.server.server.plugin.bootstrap.StandalonePluginBootstrap
import dev.slne.surf.cloud.standalone.test.TestStandaloneSpringApplication

class TestStandaloneBootstrap : StandalonePluginBootstrap {
    override suspend fun bootstrap(context: BootstrapContext) {
        repeat(20) {
            println("# Bootstrapping $it")
        }

        cloudInstance.startSpringApplication(TestStandaloneSpringApplication::class)
    }
}