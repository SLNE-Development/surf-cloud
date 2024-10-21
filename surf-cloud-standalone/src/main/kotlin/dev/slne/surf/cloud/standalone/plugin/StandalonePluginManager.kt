package dev.slne.surf.cloud.standalone.plugin

import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.api.util.mutableObjectSetOf
import dev.slne.surf.cloud.api.util.synchronize
import java.nio.file.Path


object StandalonePluginManager : Runnable{
    private val log = logger()
    private val plugins = mutableObjectSetOf<StandalonePlugin>().synchronize()

    val PLUGIN_DIRECTORY: Path = Path.of("plugins")

    fun addPlugin(plugin: StandalonePlugin) {
        plugins.add(plugin)
    }

    override fun run() {
        stopPlugins()
    }

    private fun stopPlugins() {
        for (plugin in plugins) {
            try {
                plugin.stop()
            } catch (e: Exception) {
                log.atSevere()
                    .withCause(e)
                    .log("Failed to stop plugin '%s'", plugin.id)
            }
        }
    }
}
