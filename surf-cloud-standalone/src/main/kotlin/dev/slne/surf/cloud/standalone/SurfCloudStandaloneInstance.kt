package dev.slne.surf.cloud.standalone

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.SurfCloudInstance
import dev.slne.surf.cloud.api.server.server.plugin.PluginManager
import dev.slne.surf.cloud.api.server.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.core.common.SurfCloudCoreInstance
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.cloud.core.common.util.random
import dev.slne.surf.cloud.standalone.netty.server.StandaloneNettyManager
import dev.slne.surf.cloud.standalone.netty.server.network.ServerEncryptionManager
import dev.slne.surf.cloud.standalone.plugin.PluginInitializerManager
import dev.slne.surf.cloud.standalone.plugin.entrypoint.Entrypoint
import dev.slne.surf.cloud.standalone.plugin.entrypoint.LaunchEntryPointHandler
import dev.slne.surf.cloud.standalone.redis.RedisEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.data.redis.core.ReactiveRedisTemplate
import reactor.util.Loggers
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds

@AutoService(SurfCloudInstance::class)
class SurfCloudStandaloneInstance : SurfCloudCoreInstance(StandaloneNettyManager) {
    override val springProfile = "independent"

    init {
        checkInstantiationByServiceLoader()
    }

    override suspend fun preBootstrap() {
        super.preBootstrap()
        ServerEncryptionManager.init()

        PluginInitializerManager.load()
    }

    override suspend fun bootstrap(data: BootstrapData) {
        super.bootstrap(data)
        LaunchEntryPointHandler.enterBootstrappers()
    }

    override suspend fun onLoad() {
//        SpringApplication.getShutdownHandlers().add(StandalonePluginManager)
        thread(name = "KeepAlive") { while (true) runBlocking { delay(5.seconds) } }
        super.onLoad()
        random

        loadPlugins()
    }

    override suspend fun onEnable() {
        super.onEnable()

        enablePlugins()
        afterStart()

        log.atInfo()
            .log("Standalone instance is ready!")
    }

    override suspend fun onDisable() {
        disablePlugins()
        super.onDisable()
    }

    private suspend fun loadPlugins() {
        LaunchEntryPointHandler.enter(Entrypoint.SPRING_PLUGIN)
    }

    private suspend fun enablePlugins() {
        val plugins = PluginManager.instance.getPlugins()

        for (plugin in plugins) {
            if (!plugin.enabled) {
                enablePlugin(plugin)
            }
        }
    }

    private suspend fun enablePlugin(plugin: StandalonePlugin) {
        try {
            PluginManager.instance.enablePlugin(plugin)
        } catch (e: Throwable) {
            log.atSevere()
                .withCause(e)
                .log("${e.message} while enabling plugin ${plugin.meta.displayName}")
        }
    }

    suspend fun disablePlugins() {
        PluginManager.instance.disablePlugins()
    }

    private val redisEventLog = Loggers.getLogger("RedisEvent")
    fun callRedisEvent(event: RedisEvent) {
        val template = bean<ReactiveRedisTemplate<String, Any>>("reactiveRedisTemplate")
        for (channel in event.channels) {
            template.convertAndSend(channel, event)
                .log(redisEventLog)
                .subscribe()
        }
    }

    companion object {
        fun get() = SurfCloudInstance.instance as SurfCloudStandaloneInstance
    }
}

val standaloneCloudInstance
    get() = SurfCloudStandaloneInstance.get()