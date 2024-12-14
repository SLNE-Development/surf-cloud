package dev.slne.surf.cloud.bukkit

import dev.slne.surf.cloud.bukkit.netty.BukkitNettyManager
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import kotlinx.coroutines.runBlocking

@Suppress("UnstableApiUsage")
class BukkitBootstrap: PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) = runBlocking {
        BukkitNettyManager.bootstrap()
    }
}