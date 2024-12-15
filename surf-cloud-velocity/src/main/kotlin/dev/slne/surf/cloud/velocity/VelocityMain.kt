package dev.slne.surf.cloud.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Continuation
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.PluginManager
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.cloud.core.common.SurfCloudCoreInstance
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.file.Path

@Plugin(
    id = "surf-cloud-velocity",
    name = "Surf Data Velocity",
    version = "1.21.1-1.0.0-SNAPSHOT",
    description = "A cloud plugin for Velocity",
    authors = ["twisti"],
    dependencies = [Dependency("surf-velocity-api", false), Dependency("luckperms", false)]
)
class VelocityMain @Inject constructor(
    val server: ProxyServer,
    val pluginManager: PluginManager,
    val eventManager: EventManager,
    @DataDirectory val dataPath: Path
) {
    init {
        instance = this
        eventManager.register(this, this)
        runBlocking {
            velocityCloudInstance.bootstrap(SurfCloudCoreInstance.BootstrapData(
                dataFolder = dataPath
            ))
            velocityCloudInstance.onLoad()
        }
    }

    @Subscribe
    suspend fun onProxyInitialize(ignored: ProxyInitializeEvent?) {
        velocityCloudInstance.onEnable()
    }

    @Subscribe
    suspend fun onProxyShutdown(ignored: ProxyShutdownEvent?) {
        velocityCloudInstance.onDisable()
    }

    companion object {
        lateinit var instance: VelocityMain
    }
}

val proxy get() = VelocityMain.instance.server