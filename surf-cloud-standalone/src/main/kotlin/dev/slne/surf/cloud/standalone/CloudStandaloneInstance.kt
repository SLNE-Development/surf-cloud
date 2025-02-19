package dev.slne.surf.cloud.standalone

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.api.server.CloudServerInstance
import dev.slne.surf.cloud.api.server.export.RootExportPlayerData
import dev.slne.surf.cloud.api.server.plugin.PluginManager
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.core.common.CloudCoreInstance
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundTriggerShutdownPacket
import dev.slne.surf.cloud.core.common.server.CommonCloudServerImpl
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.cloud.core.common.util.random
import dev.slne.surf.cloud.standalone.ktor.KtorServer
import dev.slne.surf.cloud.standalone.netty.server.StandaloneNettyManager
import dev.slne.surf.cloud.standalone.netty.server.network.ServerEncryptionManager
import dev.slne.surf.cloud.standalone.player.standalonePlayerManagerImpl
import dev.slne.surf.cloud.standalone.plugin.PluginInitializerManager
import dev.slne.surf.cloud.standalone.plugin.entrypoint.Entrypoint
import dev.slne.surf.cloud.standalone.plugin.entrypoint.LaunchEntryPointHandler
import dev.slne.surf.cloud.standalone.server.connection
import kotlinx.coroutines.*
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.codecs.EncoderContext
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import java.util.*

@AutoService(CloudInstance::class)
class CloudStandaloneInstance : CloudCoreInstance(StandaloneNettyManager),
    CloudServerInstance {
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
        super.onLoad()
        random

        loadPlugins()
    }

    override suspend fun onEnable() {
        super.onEnable()

        enablePlugins()
        KtorServer.start()
        afterStart()

        log.atInfo()
            .log("Standalone instance is ready!")
    }

    override suspend fun onDisable() {
        disablePlugins()
        super.onDisable()
    }

    override fun shutdownServer(server: CommonCloudServerImpl) {
        server.connection.send(ClientboundTriggerShutdownPacket)
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

    private val playerDataCodec =
        KotlinSerializerCodec.create<RootExportPlayerData>() ?: error("Codec not found")

    override suspend fun exportPlayerData(uuid: UUID): String = supervisorScope {
        val standaloneData = coroutineScope {
            async(Dispatchers.IO) {
                standalonePlayerManagerImpl.exportPlayerData(uuid)
            }
        }

        val pluginData = PluginManager.instance.getPlugins().map { plugin ->
            coroutineScope {
                async(Dispatchers.IO) {
                    runCatching {
                        plugin.exportPlayerData(uuid)
                    }.getOrElse {
                        log.atSevere()
                            .withCause(it)
                            .log("Error while exporting player data for plugin ${plugin.meta.displayName}")
                        null
                    }
                }
            }
        }

        val dataExport = (listOf(standaloneData) + pluginData).awaitAll().filterNotNull()
        val data = RootExportPlayerData(dataExport)

        val document = BsonDocument()
        playerDataCodec.encode(BsonDocumentWriter(document), data, EncoderContext.builder().build())
        document.toJson()
    }

    override suspend fun deleteNotInterestingPlayerData(uuid: UUID): Unit = supervisorScope {
        val standaloneDelete = coroutineScope {
            async(Dispatchers.IO) {
                standalonePlayerManagerImpl.deleteNotInterestingPlayerData(uuid)
            }
        }

        val pluginDelete = PluginManager.instance.getPlugins().map { plugin ->
            coroutineScope {
                async(Dispatchers.IO) {
                    runCatching {
                        plugin.deleteNotInterestingPlayerData(uuid)
                    }.getOrElse {
                        log.atSevere()
                            .withCause(it)
                            .log("Error while deleting player data for plugin ${plugin.meta.displayName}")
                    }
                }
            }
        }

        standaloneDelete.await()
        pluginDelete.awaitAll()
    }

    companion object {
        fun get() = CloudInstance.instance as CloudStandaloneInstance
    }
}

val standaloneCloudInstance
    get() = CloudStandaloneInstance.get()