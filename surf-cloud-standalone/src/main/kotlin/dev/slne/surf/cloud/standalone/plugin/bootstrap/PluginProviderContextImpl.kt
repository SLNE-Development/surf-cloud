package dev.slne.surf.cloud.standalone.plugin.bootstrap

import dev.slne.surf.cloud.api.server.server.plugin.bootstrap.StandalonePluginProviderContext
import dev.slne.surf.cloud.api.server.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.standalone.plugin.PluginInitializerManager
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
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