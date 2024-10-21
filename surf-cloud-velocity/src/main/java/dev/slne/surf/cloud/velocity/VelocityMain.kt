package dev.slne.surf.cloud.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.PluginManager
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path

@Plugin(
    id = "surf-cloud-velocity",
    name = "Surf Data Velocity",
    version = "1.21.1-1.0.0-SNAPSHOT",
    description = "A cloud plugin for Velocity",
    authors = ["twisti"]
)
class VelocityMain @Inject constructor(
    val server: ProxyServer,
    val pluginManager: PluginManager,
    val eventManager: EventManager,
    @DataDirectory val dataPath: Path
) {
    init {
        instance = this
        velocityCloudInstance.onLoad()
        eventManager.register(this, this)
    }

    @Subscribe
    fun onProxyInitialize(ignored: ProxyInitializeEvent?) {
        velocityCloudInstance.onEnable()
    }

    @Subscribe
    fun onProxyShutdown(ignored: ProxyShutdownEvent?) {
        velocityCloudInstance.onDisable()
    }

    companion object {
        lateinit var instance: VelocityMain
    }
}
