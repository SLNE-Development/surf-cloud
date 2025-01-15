package dev.slne.surf.cloud.standalone.plugin.bootstrap

import dev.slne.surf.cloud.api.server.plugin.bootstrap.BootstrapContext
import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import java.nio.file.Path
import kotlin.io.path.div

data class PluginBootstrapContextImpl(
    override val meta: PluginMeta,
    override val dataPath: Path,
    override val pluginSource: Path
) : BootstrapContext {
    companion object {
        fun create(provider: PluginProvider<*>, pluginFolder: Path) = PluginBootstrapContextImpl(
            provider.meta,
            pluginFolder / provider.meta.name,
            pluginFolder
        )
    }
}