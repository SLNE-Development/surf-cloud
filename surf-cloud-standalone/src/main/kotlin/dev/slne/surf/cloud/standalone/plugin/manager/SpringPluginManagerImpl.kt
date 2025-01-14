package dev.slne.surf.cloud.standalone.plugin.manager

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.server.server.plugin.PluginManager
import dev.slne.surf.cloud.api.server.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.api.server.server.plugin.dependency.DependencyContext
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader

@AutoService(PluginManager::class)
class SpringPluginManagerImpl : PluginManager, DependencyContext {

    private val instanceManager = StandalonePluginInstanceManager()

    init {
        checkInstantiationByServiceLoader()
    }

    override fun getPlugin(name: String) = instanceManager.getPlugin(name)
    override fun getPlugins() = instanceManager.getPlugins()
    override fun isPluginEnabled(name: String) = instanceManager.isPluginEnabled(name)
    override fun isPluginEnabled(plugin: StandalonePlugin?) =
        instanceManager.isPluginEnabled(plugin)

    override suspend fun disablePlugins() = instanceManager.disablePlugins()
    override suspend fun clearPlugins() = instanceManager.clearPlugins()
    override suspend fun enablePlugin(plugin: StandalonePlugin) =
        instanceManager.enablePlugin(plugin)

    override suspend fun disablePlugin(plugin: StandalonePlugin) =
        instanceManager.disablePlugin(plugin)

    override fun isTransitiveDependency(
        plugin: PluginMeta,
        dependency: PluginMeta
    ) = instanceManager.isTransitiveDepend(plugin, dependency)

    override fun hasDependency(pluginId: String) = instanceManager.hasDependency(pluginId)

    fun loadPlugin(plugin: StandalonePlugin) = instanceManager.loadPlugin(plugin)
}