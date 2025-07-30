package dev.slne.surf.cloud.standalone.plugin

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.api.server.plugin.PluginManager
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.core.common.CloudCoreInstance
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.cloud.standalone.plugin.entrypoint.Entrypoint
import dev.slne.surf.cloud.standalone.plugin.entrypoint.LaunchEntryPointHandler
import dev.slne.surf.cloud.standalone.plugin.provider.impl.StandalonePluginParent.StandalonePluginProvider
import dev.slne.surf.cloud.standalone.plugin.provider.source.DirectoryProviderSource
import dev.slne.surf.cloud.standalone.plugin.util.EntrypointUtil
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.CompletableDeferred
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.io.path.Path

@Component
@Order(CloudLifecycleAware.PLUGIN_MANAGER_PRIORITY)
class PluginInitializerManager: CloudLifecycleAware {
    val pluginsEnabledDeferred = CompletableDeferred<Unit>()

    companion object {
        private val log = logger()
        val pluginDirectoryPath = Path("plugins")
    }

    override suspend fun onBootstrap(
        data: CloudCoreInstance.BootstrapData,
        timeLogger: TimeLogger
    ) {
        timeLogger.measureStep("Initializing plugins") {
            load()
        }

        timeLogger.measureStep("Enter plugin bootstrappers") {
            LaunchEntryPointHandler.enterBootstrappers()
        }
    }

    override suspend fun onLoad(timeLogger: TimeLogger) {
        timeLogger.measureStep("Loading plugins") {
            LaunchEntryPointHandler.enter(Entrypoint.SPRING_PLUGIN)
        }
    }

    override suspend fun onEnable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Enabling plugins") {
            enablePlugins()
            pluginsEnabledDeferred.complete(Unit)
        }
    }

    override suspend fun onDisable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Disabling plugins") {
            disablePlugins()
        }
    }

    fun load() {
        log.atInfo().log("Initializing plugins...")

        EntrypointUtil.registerProvidersFromSource(DirectoryProviderSource, pluginDirectoryPath)
        val pluginNames = LaunchEntryPointHandler.storage
            .flatMap { it.value.getRegisteredProviders() }
            .filterIsInstance<StandalonePluginProvider>()
            .map { "${it.meta.name} (${it.meta.version})" }
        val total = pluginNames.size

        log.atInfo()
            .log("Initialized $total plugin${if (total != 1) "s" else ""}")

        if (pluginNames.isNotEmpty()) {
            log.atInfo().log("Spring Plugins: \n - ${pluginNames.joinToString("\n - ")}")
        }
    }

    private suspend fun enablePlugins() {
        val plugins = PluginManager.instance.getPlugins()

        for (plugin in plugins) {
            if (!plugin.enabled) {
                enablePlugin(plugin)
            }
        }
    }

    private suspend fun enablePlugin(plugin: StandalonePlugin) {
        try {
            PluginManager.instance.enablePlugin(plugin)
        } catch (e: Throwable) {
            log.atSevere()
                .withCause(e)
                .log("${e.message} while enabling plugin ${plugin.meta.displayName}")
        }
    }

    suspend fun disablePlugins() {
        PluginManager.instance.disablePlugins()
    }
}