package dev.slne.surf.cloud.standalone.plugin.entrypoint

import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider

interface EntrypointHandler {
    fun <T> register(entrypoint: Entrypoint<T>, provider: PluginProvider<T>)
    suspend fun enter(entrypoint: Entrypoint<*>)
}