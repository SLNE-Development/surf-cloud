package dev.slne.surf.cloud.bukkit

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import kotlinx.coroutines.runBlocking

@Suppress("UnstableApiUsage", "unused")
class BukkitBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) = runBlocking {
        bukkitCloudInstance.bootstrap()
    }
}