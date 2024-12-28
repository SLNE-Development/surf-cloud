package dev.slne.surf.cloud.core.common.player

import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerConnectToNetworkEvent
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.core.common.util.publish
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.MustBeInvokedByOverriders
import java.util.*

abstract class CloudPlayerManagerImpl<P : CommonCloudPlayerImpl> : CloudPlayerManager {
    private val players = mutableObject2ObjectMapOf<UUID, P>().synchronize()

    override fun getPlayer(uuid: UUID?): P? {
        return players[uuid]
    }

    abstract suspend fun createPlayer(uuid: UUID, serverUid: Long, proxy: Boolean): P

    abstract suspend fun updateProxyServer(player: P, serverUid: Long)
    abstract suspend fun updateServer(player: P, serverUid: Long)

    abstract suspend fun removeProxyServer(player: P, serverUid: Long)
    abstract suspend fun removeServer(player: P, serverUid: Long)

    abstract fun getProxyServerUid(player: P): Long?
    abstract fun getServerUid(player: P): Long?

    private fun addPlayer(player: P) {
        players[player.uuid] = player
    }

    protected fun forEachPlayer(action: (P) -> Unit) {
        val tempPlayers = Object2ObjectArrayMap(players)
        tempPlayers.values.forEach(action)
        tempPlayers.clear()
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
                val createPlayer = createPlayer(uuid, serverUid, true)
                onConnect(uuid, createPlayer)
                addPlayer(createPlayer)
            } else {
                updateProxyServer(player, serverUid)
            }
        } else {
            if (player == null) {
                val createPlayer = createPlayer(uuid, serverUid, false)
                onConnect(uuid, createPlayer)
                addPlayer(createPlayer)
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
        val oldProxy = getProxyServerUid(player)
        val oldServer = getServerUid(player)

        if (proxy) {
            removeProxyServer(player, serverUid)
        } else {
            removeServer(player, serverUid)
        }
        onServerDisconnect(uuid, player, serverUid)

        if (!player.connected) {
            players.remove(uuid)
            onNetworkDisconnect(uuid, player, oldProxy, oldServer)
        }
    }

    @MustBeInvokedByOverriders
    @ApiStatus.OverrideOnly
    open suspend fun onServerDisconnect(uuid: UUID, player: P, serverUid: Long) {
    }

    @MustBeInvokedByOverriders
    @ApiStatus.OverrideOnly
    open suspend fun onNetworkDisconnect(uuid: UUID, player: P, oldProxy: Long?, oldServer: Long?) {
    }

    @MustBeInvokedByOverriders
    @ApiStatus.OverrideOnly
    open suspend fun onConnect(uuid: UUID, player: P) {
        CloudPlayerConnectToNetworkEvent(this, player).publish()
    }
}

val playerManagerImpl get() = CloudPlayerManager.instance as CloudPlayerManagerImpl<*>