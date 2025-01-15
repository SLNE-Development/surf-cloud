package dev.slne.surf.cloud.api.server.plugin.dependency

import dev.slne.surf.cloud.api.common.util.InternalApi
import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta

@InternalApi
interface DependencyContext {
    fun isTransitiveDependency(plugin: PluginMeta, dependency: PluginMeta): Boolean
    fun hasDependency(pluginId: String): Boolean
}