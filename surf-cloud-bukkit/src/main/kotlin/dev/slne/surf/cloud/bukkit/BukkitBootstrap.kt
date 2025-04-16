package dev.slne.surf.cloud.bukkit

import dev.slne.surf.cloud.core.common.CloudCoreInstance.BootstrapData
import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.cloud.core.common.handleEventuallyFatalError
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin
import kotlin.system.exitProcess

@Suppress("UnstableApiUsage", "unused")
class BukkitBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext): Unit = runBlocking {
        try {
            coreCloudInstance.bootstrap(
                BootstrapData(
                    dataFolder = context.dataDirectory
                )
            )
        } catch (e: Throwable) {
            e.handleEventuallyFatalError {
                exitProcess(it)
            }
        }
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return BukkitMain()
    }
}