@file:OptIn(InternalApi::class)

package dev.slne.surf.cloud.api.server.plugin

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.cloud.api.server.export.PlayerDataExport
import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.api.server.plugin.coroutine.CoroutineManager
import dev.slne.surf.cloud.api.server.plugin.provider.classloader.SpringPluginClassloader
import kotlinx.coroutines.*
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import java.nio.file.Path
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

abstract class StandalonePlugin {
    var enabled = false
        private set
    lateinit var meta: PluginMeta
        private set
    lateinit var dataFolder: Path
        private set
    lateinit var classLoader: ClassLoader
        private set

    val logger = ComponentLogger.logger(javaClass)
    val scope get() = CoroutineManager.instance.getCoroutineSession(this).scope
    val dispatcher get() = CoroutineManager.instance.getCoroutineSession(this).dispatcher

    init {
        val classLoader = this::class.java.classLoader
        require(classLoader is SpringPluginClassloader) { "StandalonePlugin must be loaded by SpringPluginClassloader" }
        classLoader.init(this)
    }

    abstract suspend fun load()
    abstract suspend fun enable()
    abstract suspend fun disable()
    abstract suspend fun exportPlayerData(uuid: UUID): PlayerDataExport
    abstract suspend fun deleteNotInterestingPlayerData(uuid: UUID)

    @InternalApi
    fun init(
        meta: PluginMeta,
        dataFolder: Path,
        classLoader: ClassLoader
    ) {
        this.meta = meta
        this.dataFolder = dataFolder
        this.classLoader = classLoader
        CoroutineManager.instance.setupCoroutineSession(this)
    }

    @InternalApi
    suspend fun setEnabled(enabled: Boolean) {
        if (this.enabled == enabled) return
        this.enabled = enabled

        if (enabled) {
            enable()
        } else {
            disable()
        }
    }

    fun launch(
        context: CoroutineContext = dispatcher,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        if (!scope.isActive) {
            return Job()
        }

        return scope.launch(context, start, block)
    }

    companion object {
        inline fun <reified P : StandalonePlugin> getPlugin(clazz: KClass<P> = P::class): P {
            require(StandalonePlugin::class.java.isAssignableFrom(clazz.java)) { "Plugin class must be a subclass of StandalonePlugin" }
            val classLoader = clazz.java.classLoader

            require(classLoader is SpringPluginClassloader) { "Plugin class must be loaded by SpringPluginClassloader" }
            val plugin = classLoader.plugin ?: error("Plugin not initialized")

            require(plugin is P) { "Plugin is not an instance of ${clazz.simpleName}" }
            return plugin
        }
    }
}