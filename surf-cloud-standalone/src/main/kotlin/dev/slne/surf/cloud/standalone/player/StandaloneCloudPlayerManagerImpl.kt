package dev.slne.surf.cloud.standalone.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.server.export.PlayerDataExport
import dev.slne.surf.cloud.api.server.export.PlayerDataExportEmpty
import dev.slne.surf.cloud.core.common.coroutines.PlayerDataSaveScope
import dev.slne.surf.cloud.core.common.player.CloudPlayerManagerImpl
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.cloud.standalone.persistent.PlayerDataStorage
import dev.slne.surf.cloud.standalone.player.db.service.CloudPlayerService
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneProxyCloudServerImpl
import dev.slne.surf.cloud.standalone.server.asStandaloneServer
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.*
import org.gradle.internal.impldep.kotlinx.coroutines.awaitAll
import java.util.*
import kotlin.time.Duration.Companion.minutes

@AutoService(CloudPlayerManager::class)
class StandaloneCloudPlayerManagerImpl : CloudPlayerManagerImpl<StandaloneCloudPlayerImpl>() {
    private val log = logger()

    init {
        checkInstantiationByServiceLoader()
        PlayerDataSaveScope.launch { createPlayerDataSaveTask() }
    }

//    override fun terminate() = runBlocking {
//        super.terminate()
//        saveJob.cancelAndJoin()
//    }

    private suspend fun createPlayerDataSaveTask() = coroutineScope {
        while (isActive) {
            delay(3.minutes)
            forEachPlayer { PlayerDataStorage.save(it) }
        }
    }

    override suspend fun createPlayer(
        uuid: UUID,
        serverUid: Long,
        proxy: Boolean
    ): StandaloneCloudPlayerImpl {
        return StandaloneCloudPlayerImpl(uuid).also {
            val server = serverUid.toServer()
            if (server != null) {
                if (proxy) {
                    check(server is StandaloneProxyCloudServerImpl) { "Server with id $serverUid is not a proxy server but specified as proxy" }
                    it.proxyServer = server
                } else {
                    check(server is StandaloneCloudServerImpl) { "Server with id $serverUid is not a standalone server but specified as standalone" }
                    it.server = server
                }
            } else {
                log.atWarning()
                    .log("Could not find server with id $serverUid for player $uuid")
            }
        }
    }

    override suspend fun updateProxyServer(player: StandaloneCloudPlayerImpl, serverUid: Long) {
        val server = serverUid.toServer()
        check(server == null || server is StandaloneProxyCloudServerImpl) { "Server with id $serverUid is not a proxy server but specified as proxy" }

        if (server != null) {
            player.proxyServer = server
        } else {
            logServerNotFound(serverUid, player)
        }
    }

    override suspend fun updateServer(player: StandaloneCloudPlayerImpl, serverUid: Long) {
        val server = serverUid.toServer()
        check(server == null || server is StandaloneCloudServerImpl) { "Server with id $serverUid is not a standalone server but specified as standalone" }

        if (server != null) {
            player.server = server
        } else {
            logServerNotFound(serverUid, player)
        }
    }

    override suspend fun removeProxyServer(player: StandaloneCloudPlayerImpl, serverUid: Long) {
        val server = serverUid.toServer()
        check(server == null || server is StandaloneProxyCloudServerImpl) { "Server with id $serverUid is not a proxy server but specified as proxy" }

        if (server != null && player.proxyServer == server) {
            player.proxyServer = null
        } else {
            logServerNotFound(serverUid, player)
        }
    }

    override suspend fun removeServer(player: StandaloneCloudPlayerImpl, serverUid: Long) {
        val server = serverUid.toServer()
        check(server == null || server is StandaloneCloudServerImpl) { "Server with id $serverUid is not a standalone server but specified as standalone" }

        if (server != null && player.server == server) {
            player.server = null
        } else {
            logServerNotFound(serverUid, player)
        }
    }

    override fun getProxyServerUid(player: StandaloneCloudPlayerImpl) = player.proxyServer?.uid
    override fun getServerUid(player: StandaloneCloudPlayerImpl) = player.server?.uid

    override fun getOfflinePlayer(uuid: UUID): OfflineCloudPlayer {
        return OfflineCloudPlayerImpl(uuid)
    }

    override suspend fun onServerConnect(
        uuid: UUID,
        player: StandaloneCloudPlayerImpl,
        serverUid: Long
    ) {
        supervisorScope {
            awaitAll(
                async {
                    bean<CloudPlayerService>().updateOnServerConnect(player)
                }
            )
        }

        super.onServerConnect(uuid, player, serverUid)
    }

    override suspend fun onNetworkConnect(
        uuid: UUID,
        player: StandaloneCloudPlayerImpl
    ) {

        supervisorScope {
            awaitAll(
                async(PlayerDataSaveScope.context) {
                    PlayerDataStorage.load(player)
                }
            )
        }

        super.onNetworkConnect(uuid, player)
    }

    override suspend fun onNetworkDisconnect(
        uuid: UUID,
        player: StandaloneCloudPlayerImpl,
        oldProxy: Long?,
        oldServer: Long?
    ) {
        super.onNetworkDisconnect(uuid, player, oldProxy, oldServer)

        supervisorScope {
            awaitAll(
                async {
                    oldServer?.toServer()?.asStandaloneServer()?.queue?.handlePlayerLeave(player)
                },
                async(PlayerDataSaveScope.context) {
                    PlayerDataStorage.save(player)
                },
                async {
                    bean<CloudPlayerService>().updateOnDisconnect(player)
                }
            )
        }
    }

    private suspend fun Long.toServer() = serverManagerImpl.retrieveServerById(this)

    suspend fun exportPlayerData(uuid: UUID): PlayerDataExport {
        return PlayerDataExportEmpty
    }

    suspend fun deleteNotInterestingPlayerData(uuid: UUID) {

    }

    private fun logServerNotFound(uid: Long, player: StandaloneCloudPlayerImpl) {
        log.atWarning()
            .log("Could not find server with id $uid for player ${player.uuid}")
    }
}

val standalonePlayerManagerImpl get() = playerManagerImpl as StandaloneCloudPlayerManagerImpl