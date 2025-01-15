package dev.slne.surf.cloud.standalone.plugin.bootstrap

import dev.slne.surf.cloud.api.server.plugin.bootstrap.StandalonePluginProviderContext
import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.standalone.plugin.PluginInitializerManager
import java.nio.file.Path
import kotlin.io.path.div

data class PluginProviderContextImpl(
    override val meta: PluginMeta,
    override val dataPath: Path,
    override val pluginSource: Path
) : StandalonePluginProviderContext {
    companion object {
        fun create(meta: PluginMeta, pluginSource: Path) = PluginProviderContextImpl(
            meta,
            PluginInitializerManager.pluginDirectoryPath / meta.name,
            pluginSource
        )
    }
}