package dev.slne.surf.cloud.api.server.plugin.provider.classloader

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import org.springframework.context.ConfigurableApplicationContext
import java.io.Closeable
import java.nio.file.Path

@InternalApi
interface SpringPluginClassloader : Closeable {
    val meta: PluginMeta
    val plugin: StandalonePlugin?
    val group: SpringPluginClassloaderGroup?
    val source: Path
    val context: ConfigurableApplicationContext

    fun loadClass(
        name: String,
        resolve: Boolean,
        checkGlobal: Boolean,
        checkLibraries: Boolean
    ): Class<*>

    fun init(plugin: StandalonePlugin)
}