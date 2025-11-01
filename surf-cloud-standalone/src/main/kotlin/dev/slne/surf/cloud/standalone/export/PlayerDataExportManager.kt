package dev.slne.surf.cloud.standalone.export

import dev.slne.surf.cloud.api.server.export.RootExportPlayerData
import dev.slne.surf.cloud.api.server.plugin.PluginManager
import dev.slne.surf.cloud.standalone.player.standalonePlayerManagerImpl
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.*
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.codecs.EncoderContext
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import org.springframework.stereotype.Component
import java.util.*

@Component
class PlayerDataExportManager {

    private val log = logger()
    private val playerDataCodec =
        KotlinSerializerCodec.create<RootExportPlayerData>() ?: error("Codec not found")

    suspend fun exportPlayerData(uuid: UUID): String = supervisorScope {
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

    suspend fun deleteNotInterestingPlayerData(uuid: UUID): Unit = supervisorScope {
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
}