@file:OptIn(InternalPluginApi::class)

package dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy

import dev.slne.surf.cloud.api.server.server.plugin.InternalPluginApi
import dev.slne.surf.cloud.api.server.server.plugin.dependency.DependencyContext
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider

interface ProviderConfiguration<T> {
    fun applyContext(provider: PluginProvider<T>, context: DependencyContext)
    suspend fun load(provider: PluginProvider<T>, provided: T): Boolean
    fun preloadProvider(provider: PluginProvider<T>): Boolean = true
    fun onGenericError(provider: PluginProvider<T>) {}
}