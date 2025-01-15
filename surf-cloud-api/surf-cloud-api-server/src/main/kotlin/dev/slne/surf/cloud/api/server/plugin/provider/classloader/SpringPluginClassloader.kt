package dev.slne.surf.cloud.api.server.plugin.provider.classloader

import dev.slne.surf.cloud.api.common.util.InternalApi
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import java.io.Closeable

@InternalApi
interface SpringPluginClassloader : Closeable {
    val meta: PluginMeta
    val plugin: StandalonePlugin?
    val group: SpringPluginClassloaderGroup?

    fun loadClass(
        name: String,
        resolve: Boolean,
        checkGlobal: Boolean,
        checkLibraries: Boolean
    ): Class<*>

    fun init(plugin: StandalonePlugin)
}