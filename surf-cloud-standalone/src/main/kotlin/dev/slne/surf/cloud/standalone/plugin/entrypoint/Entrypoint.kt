package dev.slne.surf.cloud.standalone.plugin.entrypoint

import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.plugin.bootstrap.StandalonePluginBootstrap

class Entrypoint<T>(
    val debugName: String
) {
    companion object {
        val SPRING_PLUGIN = Entrypoint<StandalonePlugin>("spring-plugin")
        val SPRING_PLUGIN_BOOTSTRAPPER =
            Entrypoint<StandalonePluginBootstrap>("spring-plugin-bootstrapper")
    }
}