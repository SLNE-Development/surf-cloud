package dev.slne.surf.cloud.core.common.player

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import java.util.*

abstract class CloudPlayerManagerImpl : CloudPlayerManager {
    private val players = mutableObject2ObjectMapOf<UUID, CloudPlayer>().synchronize()

    override fun getPlayer(uuid: UUID?): CloudPlayer? {
        return players[uuid]
    }

    abstract suspend fun createPlayer(uuid: UUID, serverUid: Long, proxy: Boolean): CloudPlayer

    abstract suspend fun updateProxyServer(player: CloudPlayer, serverUid: Long)
    abstract suspend fun updateServer(player: CloudPlayer, serverUid: Long)

    abstract suspend fun removeProxyServer(player: CloudPlayer, serverUid: Long)
    abstract suspend fun removeServer(player: CloudPlayer, serverUid: Long)

    private fun addPlayer(player: CloudPlayer) {
        players[player.uuid] = player
    }

    /**
     * Updates the server or proxy information of an existing player,
     * or creates a new player if one does not exist.
     *
     * @param uuid The UUID of the player to update or create.
     * @param serverUid The unique identifier of the server the player is connecting to.
     * @param proxy A boolean indicating if the player is connecting through a proxy.
     */
    suspend fun updateOrCreatePlayer(uuid: UUID, serverUid: Long, proxy: Boolean) {
        val player = players[uuid]

        if (proxy) {
            if (player == null) {
                addPlayer(createPlayer(uuid, serverUid, true))
            } else {
                updateProxyServer(player, serverUid)
            }
        } else {
            if (player == null) {
                addPlayer(createPlayer(uuid, serverUid, false))
            } else {
                updateServer(player, serverUid)
            }
        }
    }

    /**
     * Updates the server or proxy information for a given player upon disconnecting,
     * or removes the player if they are no longer connected to any server or proxy.
     *
     * @param uuid The unique identifier of the player.
     * @param serverUid The unique identifier of the server.
     * @param proxy A boolean indicating if the player was connected through a proxy.
     */
    suspend fun updateOrRemoveOnDisconnect(uuid: UUID, serverUid: Long, proxy: Boolean) {
        val player = players[uuid] ?: return

        if (proxy) {
            removeProxyServer(player, serverUid)
        } else {
            removeServer(player, serverUid)
        }

        if (!player.connected) {
            players.remove(uuid)
        }
    }
}

val playerManagerImpl get() = CloudPlayerManager.instance as CloudPlayerManagerImpl