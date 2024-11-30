package dev.slne.surf.cloud.standalone.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.player.CloudPlayerManagerImpl
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.cloud.standalone.server.StandaloneServerImplCommon
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import java.util.*

@AutoService(CloudPlayerManager::class)
class StandaloneCloudPlayerManagerImpl : CloudPlayerManagerImpl() {
    private val log = logger()

    init {
        checkInstantiationByServiceLoader()
    }

    override suspend fun createPlayer(
        uuid: UUID,
        serverUid: Long,
        proxy: Boolean
    ): CloudPlayer {
        return StandaloneCloudPlayerImpl(uuid).also {
            val server = serverManagerImpl.retrieveServerById(serverUid)
            if (server != null) {
                if (proxy) {
                    it.proxyServer = server
                } else {
                    it.server = server
                }
                server.addPlayer(it)
            } else {
                log.atWarning()
                    .log("Could not find server with id $serverUid for player $uuid")
            }
        }
    }

    override suspend fun updateProxyServer(player: CloudPlayer, serverUid: Long) {
        val (standalonePlayer, server) = getStandalonePlayerAndServer(player, serverUid)
        standalonePlayer.proxyServer?.removePlayer(standalonePlayer)

        if (server != null) {
            standalonePlayer.proxyServer = server
            server.addPlayer(standalonePlayer)
        } else {
            logServerNotFound(serverUid, standalonePlayer)
        }
    }

    override suspend fun updateServer(player: CloudPlayer, serverUid: Long) {
        val (standalonePlayer, server) = getStandalonePlayerAndServer(player, serverUid)
        standalonePlayer.server?.removePlayer(standalonePlayer)

        if (server != null) {
            standalonePlayer.server = server
            server.addPlayer(standalonePlayer)
        } else {
            logServerNotFound(serverUid, standalonePlayer)
        }
    }

    override suspend fun removeProxyServer(player: CloudPlayer, serverUid: Long) {
        val (standalonePlayer, server) = getStandalonePlayerAndServer(player, serverUid)

        if (server != null && standalonePlayer.proxyServer == server) {
            standalonePlayer.proxyServer = null
            server.removePlayer(standalonePlayer)
        } else {
            logServerNotFound(serverUid, standalonePlayer)
        }
    }

    override suspend fun removeServer(player: CloudPlayer, serverUid: Long) {
        val (standalonePlayer, server) = getStandalonePlayerAndServer(player, serverUid)

        if (server != null && standalonePlayer.server == server) {
            standalonePlayer.server = null
            server.removePlayer(standalonePlayer)
        } else {
            logServerNotFound(serverUid, standalonePlayer)
        }
    }

    private suspend fun getStandalonePlayerAndServer(
        player: CloudPlayer,
        serverUid: Long
    ): Pair<StandaloneCloudPlayerImpl, StandaloneServerImplCommon?> {
        val standalonePlayer = player as? StandaloneCloudPlayerImpl
            ?: error("Player is not a StandaloneCloudPlayerImpl")
        val server = serverManagerImpl.retrieveServerById(serverUid)
        return standalonePlayer to server
    }

    private fun logServerNotFound(serverUid: Long, standalonePlayer: StandaloneCloudPlayerImpl) {
        log.atWarning()
            .log("Could not find server with id $serverUid for player ${standalonePlayer.uuid}")
    }
}