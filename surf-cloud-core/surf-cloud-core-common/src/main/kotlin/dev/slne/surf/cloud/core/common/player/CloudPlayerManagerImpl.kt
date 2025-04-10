package dev.slne.surf.cloud.core.common.player

import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerConnectToNetworkEvent
import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerDisconnectFromNetworkEvent
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.server.UserList
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.core.common.util.publish
import dev.slne.surf.surfapi.core.api.util.logger
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import org.jetbrains.annotations.MustBeInvokedByOverriders
import java.net.Inet4Address
import java.util.*

abstract class CloudPlayerManagerImpl<P : CommonCloudPlayerImpl> : CloudPlayerManager {
    private val log = logger()
    protected val players = mutableObject2ObjectMapOf<UUID, P>().synchronize()

    override fun getPlayer(uuid: UUID?): P? {
        return players[uuid]
    }

    abstract suspend fun createPlayer(
        uuid: UUID,
        name: String,
        proxy: Boolean,
        ip: Inet4Address,
        serverUid: Long
    ): P

    abstract suspend fun updateProxyServer(player: P, serverUid: Long)
    abstract suspend fun updateServer(player: P, serverUid: Long)

    abstract suspend fun removeProxyServer(player: P, serverUid: Long)
    abstract suspend fun removeServer(player: P, serverUid: Long)

    abstract fun getProxyServerUid(player: P): Long?
    abstract fun getServerUid(player: P): Long?

    private fun addPlayer(player: P) {
        players[player.uuid] = player
    }

    override fun getOnlinePlayers(): UserList {
        return UserListImpl.of(players.values)
    }

    protected inline fun forEachPlayer(action: (P) -> Unit) {
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
    suspend fun updateOrCreatePlayer(
        uuid: UUID,
        name: String,
        proxy: Boolean,
        ip: Inet4Address,
        serverUid: Long
    ) {
        val player = players[uuid]

        if (player == null) {
            createPlayer(uuid, name, proxy, ip, serverUid).also {
                onNetworkConnect(uuid, it)
                onServerConnect(uuid, it, serverUid)
                addPlayer(it)
            }
        } else {
            if (proxy) {
                updateProxyServer(player, serverUid)
            } else {
                updateServer(player, serverUid)
            }
            onServerConnect(uuid, player, serverUid)
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

    open suspend fun onServerConnect(uuid: UUID, player: P, serverUid: Long) {
    }

    @MustBeInvokedByOverriders
    open suspend fun onServerDisconnect(uuid: UUID, player: P, serverUid: Long) {
    }

    @MustBeInvokedByOverriders
    open suspend fun onNetworkDisconnect(uuid: UUID, player: P, oldProxy: Long?, oldServer: Long?) {
        try {
            CloudPlayerDisconnectFromNetworkEvent(this, player).publish()
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to publish CloudPlayerDisconnectFromNetworkEvent")
        }
    }

    @MustBeInvokedByOverriders
    open suspend fun onNetworkConnect(uuid: UUID, player: P) {
        try {
            CloudPlayerConnectToNetworkEvent(this, player).publish()
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to publish CloudPlayerConnectToNetworkEvent")
        }
    }

    open fun terminate() {}
}

val playerManagerImpl get() = CloudPlayerManager.instance as CloudPlayerManagerImpl<*>