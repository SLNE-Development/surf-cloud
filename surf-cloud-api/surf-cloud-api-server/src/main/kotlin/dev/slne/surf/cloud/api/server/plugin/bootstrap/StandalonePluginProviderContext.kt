package dev.slne.surf.cloud.api.server.plugin.bootstrap

import dev.slne.surf.cloud.api.server.server.plugin.configuration.PluginMeta
import java.nio.file.Path

interface StandalonePluginProviderContext {
    val meta: PluginMeta
    val dataPath: Path
    val pluginSource: Path
}