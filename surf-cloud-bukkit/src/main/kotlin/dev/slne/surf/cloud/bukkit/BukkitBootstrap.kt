package dev.slne.surf.cloud.bukkit

import dev.slne.surf.cloud.core.common.SurfCloudCoreInstance
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UnstableApiUsage", "unused")
class BukkitBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) = runBlocking {
        bukkitCloudInstance.bootstrap(
            SurfCloudCoreInstance.BootstrapData(
                dataFolder = context.dataDirectory
            )
        )
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return BukkitMain()
    }
}