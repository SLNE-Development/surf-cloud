package dev.slne.surf.cloud.standalone.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.server.server.ServerCloudServer
import dev.slne.surf.cloud.core.common.player.CloudPlayerManagerImpl
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import java.util.*

@AutoService(CloudPlayerManager::class)
class StandaloneCloudPlayerManagerImpl : CloudPlayerManagerImpl() {
    private val log = logger()

    override fun createPlayer(
        uuid: UUID,
        serverUid: Long,
        proxy: Boolean
    ): CloudPlayer {
        return StandaloneCloudPlayerImpl(uuid).also {
            val server = serverManagerImpl.getServerById(serverUid) as? ServerCloudServer
            if (server != null) {
                if (proxy) {
                    it.proxyServer = server
                } else {
                    it.server = server
                }
            } else {
                log.atWarning()
                    .log("Could not find server with id $serverUid for player $uuid")
            }
        }
    }

    override fun updateProxyServer(player: CloudPlayer, serverUid: Long) {
        val (standalonePlayer, server) = getStandalonePlayerAndServer(player, serverUid)

        if (server != null) {
            standalonePlayer.proxyServer = server
        } else {
            logServerNotFound(serverUid, standalonePlayer)
        }
    }

    override fun updateServer(player: CloudPlayer, serverUid: Long) {
        val (standalonePlayer, server) = getStandalonePlayerAndServer(player, serverUid)

        if (server != null) {
            standalonePlayer.server = server
        } else {
            logServerNotFound(serverUid, standalonePlayer)
        }
    }

    override fun removeProxyServer(player: CloudPlayer, serverUid: Long) {
        val (standalonePlayer, server) = getStandalonePlayerAndServer(player, serverUid)

        if (server != null && standalonePlayer.proxyServer == server) {
            standalonePlayer.proxyServer = null
        } else {
            logServerNotFound(serverUid, standalonePlayer)
        }
    }

    override fun removeServer(player: CloudPlayer, serverUid: Long) {
        val (standalonePlayer, server) = getStandalonePlayerAndServer(player, serverUid)

        if (server != null && standalonePlayer.server == server) {
            standalonePlayer.server = null
        } else {
            logServerNotFound(serverUid, standalonePlayer)
        }
    }

    private fun getStandalonePlayerAndServer(
        player: CloudPlayer,
        serverUid: Long
    ): Pair<StandaloneCloudPlayerImpl, ServerCloudServer?> {
        val standalonePlayer = player as? StandaloneCloudPlayerImpl
            ?: error("Player is not a StandaloneCloudPlayerImpl")
        val server = serverManagerImpl.getServerById(serverUid) as? ServerCloudServer
        return standalonePlayer to server
    }

    private fun logServerNotFound(serverUid: Long, standalonePlayer: StandaloneCloudPlayerImpl) {
        log.atWarning()
            .log("Could not find server with id $serverUid for player ${standalonePlayer.uuid}")
    }
}