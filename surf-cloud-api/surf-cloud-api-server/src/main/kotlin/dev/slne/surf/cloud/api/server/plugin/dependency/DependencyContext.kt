package dev.slne.surf.cloud.api.server.plugin.dependency

import dev.slne.surf.cloud.api.server.server.plugin.InternalPluginApi
import dev.slne.surf.cloud.api.server.server.plugin.configuration.PluginMeta

@InternalPluginApi
interface DependencyContext {
    fun isTransitiveDependency(plugin: PluginMeta, dependency: PluginMeta): Boolean
    fun hasDependency(pluginId: String): Boolean
}