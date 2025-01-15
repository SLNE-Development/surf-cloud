package dev.slne.surf.cloud.standalone.plugin.manager

import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.standalone.plugin.entrypoint.Entrypoint
import dev.slne.surf.cloud.standalone.plugin.entrypoint.EntrypointHandler
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import dev.slne.surf.cloud.standalone.plugin.storage.ProviderStorage

class RuntimeSpringPluginEntrypointHandler<T : ProviderStorage<StandalonePlugin>>(val storage: T) :
    EntrypointHandler {
    override fun <T> register(
        entrypoint: Entrypoint<T>,
        provider: PluginProvider<T>
    ) {
        if (entrypoint != Entrypoint.SPRING_PLUGIN) error("Invalid entrypoint")

        @Suppress("UNCHECKED_CAST")
        storage.register(provider as PluginProvider<StandalonePlugin>)
    }

    override suspend fun enter(entrypoint: Entrypoint<*>) {
        if (entrypoint != Entrypoint.SPRING_PLUGIN) error("Invalid entrypoint")

        storage.enter()
    }
}