package dev.slne.surf.cloud.standalone.plugin.storage

import dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency.PluginMetaDependencyTree
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider

interface ProviderStorage<T> {
    fun register(provider: PluginProvider<T>)
    fun createDependencyTree(): PluginMetaDependencyTree
    suspend fun enter()
    fun getRegisteredProviders(): Iterable<PluginProvider<T>>
}