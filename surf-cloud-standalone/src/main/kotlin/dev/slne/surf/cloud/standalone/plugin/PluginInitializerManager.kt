package dev.slne.surf.cloud.standalone.plugin

import dev.slne.surf.cloud.standalone.plugin.entrypoint.LaunchEntryPointHandler
import dev.slne.surf.cloud.standalone.plugin.provider.impl.StandalonePluginParent.StandalonePluginProvider
import dev.slne.surf.cloud.standalone.plugin.provider.source.DirectoryProviderSource
import dev.slne.surf.cloud.standalone.plugin.util.EntrypointUtil
import dev.slne.surf.surfapi.core.api.util.logger
import kotlin.io.path.Path

object PluginInitializerManager {
    private val log = logger()

    val pluginDirectoryPath = Path("plugins")

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
}