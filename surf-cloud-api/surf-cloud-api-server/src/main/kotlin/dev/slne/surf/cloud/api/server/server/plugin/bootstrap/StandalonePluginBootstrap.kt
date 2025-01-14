package dev.slne.surf.cloud.api.server.server.plugin.bootstrap

import dev.slne.surf.cloud.api.server.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.server.plugin.provider.ProviderLoader

interface StandalonePluginBootstrap {
    suspend fun bootstrap(context: BootstrapContext)

    fun createPlugin(context: StandalonePluginProviderContext): StandalonePlugin =
        ProviderLoader.loadClass(
            context.meta.main,
            StandalonePlugin::class.java,
            this::class.java.classLoader
        )
}