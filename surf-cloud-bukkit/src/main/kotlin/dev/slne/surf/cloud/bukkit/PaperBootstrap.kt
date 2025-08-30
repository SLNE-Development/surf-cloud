package dev.slne.surf.cloud.bukkit

import dev.slne.surf.cloud.core.common.CloudCoreInstance.BootstrapData
import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.cloud.core.common.handleEventuallyFatalError
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin
import kotlin.system.exitProcess

@Suppress("UnstableApiUsage", "unused")
class PaperBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext): Unit = runBlocking {
        val loader = net.kyori.adventure.nbt.CompoundBinaryTag::class.java.getClassLoader()
        context.logger.info("Cloud sees CBTN from: $loader")

        if (loader is ConfiguredPluginClassLoader) {
            context.logger.info("CBTN is loaded by ConfiguredPluginClassLoader, all is good: ${loader.configuration.name}")
        } else {
            context.logger.warn("CBTN is NOT loaded by ConfiguredPluginClassLoader, things may go wrong!")
        }

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
        return PaperMain()
    }
}