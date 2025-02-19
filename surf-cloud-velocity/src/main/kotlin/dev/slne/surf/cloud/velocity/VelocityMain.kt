package dev.slne.surf.cloud.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.PluginManager
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.cloud.core.common.CloudCoreInstance
import dev.slne.surf.cloud.core.common.handleEventuallyFatalError
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.system.exitProcess

@Plugin(
    id = "surf-cloud-velocity",
    name = "Surf Cloud Velocity",
    version = "1.21.4-1.0.0-SNAPSHOT",
    description = "A cloud plugin for Velocity",
    authors = ["twisti"],
    dependencies = [Dependency("surf-api-velocity"), Dependency("luckperms")]
)
class VelocityMain @Inject constructor(
    val server: ProxyServer,
    val pluginManager: PluginManager,
    val eventManager: EventManager,
    @DataDirectory val dataPath: Path
) {
    init {
        try {
            instance = this
            runBlocking {
                velocityCloudInstance.bootstrap(
                    CloudCoreInstance.BootstrapData(
                        dataFolder = dataPath
                    )
                )
                velocityCloudInstance.onLoad()
            }
        } catch (e: Throwable) {
            e.handleEventuallyFatalError({ exitProcess(it.exitCode) })
        }
    }

    @Subscribe
    suspend fun onProxyInitialize(ignored: ProxyInitializeEvent?) {
        try {
            velocityCloudInstance.onEnable()
            velocityCloudInstance.afterStart()
        } catch (e: Throwable) {
            e.handleEventuallyFatalError({ exitProcess(it.exitCode) })
        }
    }

    @Subscribe
    suspend fun onProxyShutdown(ignored: ProxyShutdownEvent?) {
        try {
            velocityCloudInstance.onDisable()
        } catch (e: Throwable) {
            e.handleEventuallyFatalError({})
        }
    }

    companion object {
        lateinit var instance: VelocityMain
    }
}

val proxy get() = VelocityMain.instance.server
val plugin get() = VelocityMain.instance