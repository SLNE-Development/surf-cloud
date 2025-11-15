package dev.slne.surf.cloud.standalone.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.api.common.util.currentValues
import dev.slne.surf.cloud.api.server.export.PlayerDataExport
import dev.slne.surf.cloud.api.server.export.PlayerDataExportEmpty
import dev.slne.surf.cloud.core.common.coroutines.PlayerDataSaveScope
import dev.slne.surf.cloud.core.common.coroutines.PlayerDatabaseScope
import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundRunPrePlayerJoinTasksPacket
import dev.slne.surf.cloud.core.common.player.CloudPlayerManagerImpl
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import dev.slne.surf.cloud.core.common.player.task.PrePlayerJoinTaskManager
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.cloud.standalone.persistent.PlayerDataStorage
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerService
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneProxyCloudServerImpl
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObject2LongMapOf
import kotlinx.coroutines.*
import java.net.Inet4Address
import java.time.ZonedDateTime
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

    override suspend fun preJoin(player: StandaloneCloudPlayerImpl): PrePlayerJoinTask.Result {
        val serverResult = PrePlayerJoinTaskManager.runTasks(player)
        if (serverResult !is PrePlayerJoinTask.Result.ALLOWED) return serverResult
        val connections = serverManagerImpl.retrieveAllServers().map { it.connection }

        for (connection in connections) {
            val resultPacket =
                ClientboundRunPrePlayerJoinTasksPacket(player.uuid).fireAndAwait(connection)

            if (resultPacket == null) {
                log.atWarning()
                    .log("Pre player join tasks for player ${player.uuid} took too long to complete")
                return PrePlayerJoinTask.Result.ERROR
            }

            val result = resultPacket.result
            if (result !is PrePlayerJoinTask.Result.ALLOWED) {
                return result
            }
        }

        return PrePlayerJoinTask.Result.ALLOWED
    }

    private fun handleResult(
        result: PrePlayerJoinTask.Result,
        player: StandaloneCloudPlayerImpl
    ): Boolean = when (result) {
        PrePlayerJoinTask.Result.ALLOWED -> true
        is PrePlayerJoinTask.Result.DENIED -> {
            player.disconnect(result.reason)
            false
        }

        PrePlayerJoinTask.Result.ERROR -> {
            player.disconnect(MessageManager.unknownErrorDuringLogin)
            false
        }
    }


    override fun createPlayer(
        uuid: UUID,
        name: String,
        proxy: Boolean,
        ip: Inet4Address,
        serverName: String
    ): StandaloneCloudPlayerImpl {
        return StandaloneCloudPlayerImpl(uuid, name, ip).also {
            val server = serverName.toServer()
            if (server != null) {
                if (proxy) {
                    check(server is StandaloneProxyCloudServerImpl) { "Server with id $serverName is not a proxy server but specified as proxy" }
                    it.proxyServer = server
                } else {
                    check(server is StandaloneCloudServerImpl) { "Server with id $serverName is not a standalone server but specified as standalone" }
                    it.server = server
                }
            } else {
                log.atWarning()
                    .log("Could not find server with id $serverName for player $uuid")
            }
        }
    }

    override fun updateProxyServer(player: StandaloneCloudPlayerImpl, serverName: String) {
        val server = serverName.toServer()
        check(server == null || server is StandaloneProxyCloudServerImpl) { "Server with id $serverName is not a proxy server but specified as proxy" }

        if (server != null) {
            player.proxyServer = server
        } else {
            logServerNotFound(serverName, player)
        }
    }

    override fun updateServer(player: StandaloneCloudPlayerImpl, serverName: String) {
        val server = serverName.toServer()
        check(server == null || server is StandaloneCloudServerImpl) { "Server with id $serverName is not a standalone server but specified as standalone" }

        if (server != null) {
            player.server = server
        } else {
            logServerNotFound(serverName, player)
        }
    }

    override fun removeProxyServer(player: StandaloneCloudPlayerImpl, serverName: String) {
        val server = serverName.toServer()
        check(server == null || server is StandaloneProxyCloudServerImpl) { "Server with id $serverName is not a proxy server but specified as proxy" }

        if (server != null && player.proxyServer == server) {
            player.proxyServer = null
        } else {
            logServerNotFound(serverName, player)
        }
    }

    override fun removeServer(player: StandaloneCloudPlayerImpl, serverName: String) {
        val server = serverName.toServer()
        check(server == null || server is StandaloneCloudServerImpl) { "Server with id $serverName is not a standalone server but specified as standalone" }

        if (server != null && player.server == server) {
            player.server = null
        } else {
            logServerNotFound(serverName, player)
        }
    }

    override fun getProxyServerName(player: StandaloneCloudPlayerImpl): String? =
        player.proxyServer?.name

    override fun getServerName(player: StandaloneCloudPlayerImpl): String? = player.server?.name

    override fun getOfflinePlayer(uuid: UUID, createIfNotExists: Boolean): OfflineCloudPlayer {
        return OfflineCloudPlayerImpl(uuid).also {
            if (createIfNotExists) {
                PlayerDatabaseScope.launch {
                    bean<CloudPlayerService>().createIfNotExists(uuid)
                }
            }
        }
    }

    fun getRawOnlinePlayers() = playerCache.currentValues()

    override suspend fun onServerConnect(
        uuid: UUID,
        player: StandaloneCloudPlayerImpl,
        serverName: String
    ) {
        supervisorScope {
            awaitAll(
                async {
                    bean<CloudPlayerService>().updateOnServerConnect(player)
                }
            )
        }

        super.onServerConnect(uuid, player, serverName)
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

        player.sessionStartTime = ZonedDateTime.now()
        super.onNetworkConnect(uuid, player)
    }

    override suspend fun onNetworkDisconnect(
        uuid: UUID,
        player: StandaloneCloudPlayerImpl,
        oldProxy: String?,
        oldServer: String?
    ) {
        super.onNetworkDisconnect(uuid, player, oldProxy, oldServer)

        supervisorScope {
            awaitAll(
                async(PlayerDataSaveScope.context) {
                    PlayerDataStorage.save(player)
                },
                async {
                    bean<CloudPlayerService>().updateOnDisconnect(player, oldServer)
                }
            )
        }
    }

    private fun String.toServer() = serverManagerImpl.retrieveServerByName(this)

    suspend fun exportPlayerData(uuid: UUID): PlayerDataExport {
        return PlayerDataExportEmpty
    }

    suspend fun deleteNotInterestingPlayerData(uuid: UUID) {

    }

    override suspend fun terminate() {
        super.terminate()
        val sentDisconnect = mutableObject2LongMapOf<UUID>()

        while (!playerCache.underlying().asMap().isEmpty()) { // TODO: enhance
            forEachPlayer {
                val lastSent =
                    sentDisconnect.computeIfAbsent(it.uuid) { System.currentTimeMillis() }

                if (System.currentTimeMillis() - lastSent > 15.seconds.inWholeMilliseconds) {
                    it.disconnect(MessageManager.networkShutdown)
                    sentDisconnect.put(it.uuid, System.currentTimeMillis())
                }
            }
            delay(5.seconds)
        }
    }

    private fun logServerNotFound(name: String, player: StandaloneCloudPlayerImpl) {
        log.atWarning()
            .log("Could not find server '$name' for player ${player.uuid}")
    }
}

val standalonePlayerManagerImpl get() = playerManagerImpl as StandaloneCloudPlayerManagerImpl