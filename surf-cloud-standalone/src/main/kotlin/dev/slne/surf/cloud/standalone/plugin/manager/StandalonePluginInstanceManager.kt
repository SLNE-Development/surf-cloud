package dev.slne.surf.cloud.standalone.plugin.manager

import dev.slne.surf.cloud.api.server.plugin.PluginManager
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.api.server.plugin.provider.classloader.SpringPluginClassloader
import dev.slne.surf.cloud.api.server.plugin.provider.classloader.SpringPluginClassloaderStorage
import dev.slne.surf.cloud.standalone.plugin.entrypoint.Entrypoint
import dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency.PluginMetaDependencyTree
import dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency.SimplePluginMetaDependencyTree
import dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy.PluginGraphCycleException
import dev.slne.surf.cloud.standalone.plugin.exception.InvalidPluginException
import dev.slne.surf.cloud.standalone.plugin.provider.source.FileProviderSource
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import java.io.IOException
import java.nio.file.Path

class StandalonePluginInstanceManager {
    private val log = logger()

    private val fileProviderSource = FileProviderSource { "File '$it'" }

    private val plugins = mutableObjectListOf<StandalonePlugin>()
    private val lookupNames = mutableObject2ObjectMapOf<String, StandalonePlugin>()

    private val dependencyTree: PluginMetaDependencyTree = SimplePluginMetaDependencyTree()

    fun getPlugin(name: String) = lookupNames[name.replace(' ', '_').lowercase()]
    fun getPlugins() = plugins.toTypedArray()

    fun isPluginEnabled(name: String) = isPluginEnabled(getPlugin(name))

    @Synchronized
    fun isPluginEnabled(plugin: StandalonePlugin?) =
        plugin != null && plugins.contains(plugin) && plugin.enabled

    fun loadPlugin(plugin: StandalonePlugin) {
        val meta = plugin.meta

        plugins.add(plugin)
        lookupNames[meta.name.lowercase()] = plugin
        dependencyTree.add(meta)
    }

    suspend fun loadPlugin(path: Path): StandalonePlugin? {
        var path = path
        val runtimePluginEntrypointHandler = RuntimeSpringPluginEntrypointHandler(
            SingularRuntimeSpringPluginProviderStorage(dependencyTree)
        )

        try {
            path = fileProviderSource.prepareContext(path)
            fileProviderSource.registerProviders(runtimePluginEntrypointHandler, path)
        } catch (e: IllegalArgumentException) {
            return null // Return null when the plugin file is not valid / plugin type is unknown
        } catch (e: PluginGraphCycleException) {
            throw InvalidPluginException(
                "Plugin ${path.fileName} has a cyclic dependency graph!",
                e
            )
        } catch (e: Exception) {
            throw InvalidPluginException(e)
        }

        try {
            runtimePluginEntrypointHandler.enter(Entrypoint.SPRING_PLUGIN)
        } catch (e: Throwable) {
            throw InvalidPluginException(e)
        }

        return runtimePluginEntrypointHandler.storage.singleLoaded
            ?: throw InvalidPluginException("Plugin didn't load any plugin providers?")
    }

    suspend fun disablePlugins() {
        for (plugin in plugins.reversed()) {
            disablePlugin(plugin)
        }
    }

    suspend fun clearPlugins() {
        disablePlugins()
        plugins.clear()
        lookupNames.clear()
    }

    suspend fun enablePlugin(plugin: StandalonePlugin) {
        if (plugin.enabled) return

        try {
            plugin.logger.info("Enabling plugin '${plugin.meta.displayName}'...")

            val loader = plugin.javaClass.classLoader
            if (loader is SpringPluginClassloader) {
                if (SpringPluginClassloaderStorage.instance.registerUnsafePlugin(loader)) {
                    log.atWarning()
                        .log("Enabled plugin with unregistered SpringPluginClassloader '${plugin.meta.displayName}'")
                }
            }

            try {
                plugin.setEnabled(true)
            } catch (e: Throwable) {
                log.atSevere()
                    .withCause(e)
                    .log("Error enabling plugin '${plugin.meta.displayName}'")
                PluginManager.instance.disablePlugin(plugin)
                return
            }
        } catch (e: Throwable) {
            log.atSevere()
                .withCause(e)
                .log("Error enabling plugin '${plugin.meta.displayName}'")
        }
    }

    suspend fun disablePlugin(plugin: StandalonePlugin) {
        if (!plugin.enabled) return
        val displayName = plugin.meta.displayName

        try {
            plugin.logger.info("Disabling plugin '$displayName'...")

            try {
                plugin.setEnabled(false)
            } catch (e: Throwable) {
                plugin.logger.error("Error disabling plugin '$displayName'", e)
            }

            val classloader = plugin.javaClass.classLoader
            if (classloader is SpringPluginClassloader) {
                try {
                    classloader.close()
                } catch (e: IOException) {
                    plugin.logger.error("Error closing classloader for plugin '$displayName'", e)
                }

                SpringPluginClassloaderStorage.instance.unregisterClassloader(classloader)
            }
        } catch (e: Throwable) {
            plugin.logger.error("Error disabling plugin '$displayName'", e)
        }
    }

    fun isTransitiveDepend(plugin: PluginMeta, depend: PluginMeta): Boolean {
        return dependencyTree.isTransitiveDependency(plugin, depend)
    }

    fun hasDependency(pluginName: String): Boolean {
        return getPlugin(pluginName) != null
    }
}