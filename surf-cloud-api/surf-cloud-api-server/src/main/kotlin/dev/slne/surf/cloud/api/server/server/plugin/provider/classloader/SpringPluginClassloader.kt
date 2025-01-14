package dev.slne.surf.cloud.api.server.server.plugin.provider.classloader

import dev.slne.surf.cloud.api.server.server.plugin.InternalPluginApi
import dev.slne.surf.cloud.api.server.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.server.plugin.configuration.PluginMeta
import java.io.Closeable

@InternalPluginApi
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