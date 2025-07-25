package dev.slne.surf.cloud.core.common.player

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asCache
import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerConnectToNetworkEvent
import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerDisconnectFromNetworkEvent
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.api.common.server.UserList
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.api.common.util.currentValues
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.surfapi.core.api.util.logger
import org.jetbrains.annotations.MustBeInvokedByOverriders
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.Serial
import java.net.Inet4Address
import java.util.*

abstract class CloudPlayerManagerImpl<P : CommonCloudPlayerImpl> : CloudPlayerManager {
    private val log = logger()

    protected val playerCache = Caffeine.newBuilder()
        .asCache<UUID, P>()

//    protected val players = mutableObject2ObjectMapOf<UUID, P>().synchronize()
//    protected val creatingPlayers =
//        mutableObject2ObjectMapOf<UUID, CompletableDeferred<P?>>().synchronize()
//    protected val createMutex = Mutex()

    override fun getPlayer(uuid: UUID?): P? {
        return uuid?.let { playerCache.getOrNull(it) }
    }

    override fun getPlayer(name: String): CloudPlayer? =
        playerCache.currentValues().find { it.name.equals(name, ignoreCase = true) }

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

    override fun getOnlinePlayers(): UserList {
        return UserListImpl.of(playerCache.currentValues())
    }

    protected inline fun forEachPlayer(action: (P) -> Unit) {
        playerCache.currentValues().forEach(action)
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
        serverUid: Long,
        runPreJoinTasks: Boolean
    ): PrePlayerJoinTask.Result {
        val existing = playerCache.getOrNull(uuid)
        if (existing != null) {
            if (proxy) {
                updateProxyServer(existing, serverUid)
            } else {
                updateServer(existing, serverUid)
            }
            onServerConnect(uuid, existing, serverUid)
            return PrePlayerJoinTask.Result.ALLOWED
        }

        return try {
            playerCache.get(uuid) {
                val newPlayer = createPlayer(uuid, name, proxy, ip, serverUid)

                if (runPreJoinTasks) {
                    val pre = preJoin(newPlayer)
                    if (pre !is PrePlayerJoinTask.Result.ALLOWED) throw PreJoinDenied(pre)
                }
                onNetworkConnect(uuid, newPlayer)
                onServerConnect(uuid, newPlayer, serverUid)
                newPlayer
            }
            PrePlayerJoinTask.Result.ALLOWED
        } catch (e: PreJoinDenied) {
            e.result
        }


//        val (player, preJoinResult, created) = getOrCreatePlayerAtomically(
//            uuid,
//            name,
//            proxy,
//            ip,
//            serverUid,
//            runPreJoinTasks
//        )
//
//        if (player == null) {
//            return preJoinResult
//        }
//
//        if (!created) {
//            if (proxy) {
//                updateProxyServer(player, serverUid)
//            } else {
//                updateServer(player, serverUid)
//            }
//            onServerConnect(uuid, player, serverUid)
//        }
//
//        return PrePlayerJoinTask.Result.ALLOWED


//        val player = players[uuid]
//        val creatingPlayer = creatingPlayers[uuid]
//
//        if (player == null && creatingPlayer == null) {
//            val creatingPlayerDeferred = CompletableDeferred<P?>()
//            creatingPlayers[uuid] = creatingPlayerDeferred
//
//            createPlayer(uuid, name, proxy, ip, serverUid).also {
//                if (runPreJoinTasks) {
//                    val preJoinResult = preJoin(it)
//                    if (preJoinResult !is PrePlayerJoinTask.Result.ALLOWED) {
//                        creatingPlayerDeferred.complete(null)
//                        return preJoinResult
//                    }
//                }
//
//                onNetworkConnect(uuid, it)
//                onServerConnect(uuid, it, serverUid)
//                addPlayer(it)
//            }
//        } else {
//            coroutineScope {
//                if (proxy) {
//                    if (player != null) {
//                        updateProxyServer(player, serverUid)
//                    } else {
//                        launch {
//                            creatingPlayer?.await()?.let { newPlayer ->
//                                updateProxyServer(newPlayer, serverUid)
//                            }
//                        }
//                    }
//                } else {
//                    if (player != null) {
//                        updateServer(player, serverUid)
//                    } else {
//                        launch {
//                            creatingPlayer?.await()?.let { newPlayer ->
//                                updateServer(newPlayer, serverUid)
//                            }
//                        }
//                    }
//                }
//
//                if (player != null) {
//                    onServerConnect(uuid, player, serverUid)
//                } else {
//                    launch {
//                        creatingPlayer?.await()?.let { newPlayer ->
//                            onServerConnect(uuid, newPlayer, serverUid)
//                        }
//                    }
//                }
//            }
//        }
//
//        return PrePlayerJoinTask.Result.ALLOWED
    }

//    private suspend fun getOrCreatePlayerAtomically(
//        uuid: UUID,
//        name: String,
//        proxy: Boolean,
//        ip: Inet4Address,
//        serverUid: Long,
//        runPreJoinTasks: Boolean
//    ): Triple<P?, PrePlayerJoinTask.Result, Boolean> {
//        players[uuid]?.let { return Triple(it, PrePlayerJoinTask.Result.ALLOWED, false) }
//
//        var needsCreation = false
//        val creatingPlayer = createMutex.withLock {
//            creatingPlayers.computeIfAbsent(uuid) {
//                needsCreation = true
//                CompletableDeferred()
//            }
//        }
//
//        if (!creatingPlayer.isCompleted && needsCreation) {
//            val newPlayer = createPlayer(uuid, name, proxy, ip, serverUid)
//
//            if (runPreJoinTasks) {
//                val preJoinResult = preJoin(newPlayer)
//                if (preJoinResult !is PrePlayerJoinTask.Result.ALLOWED) {
//                    creatingPlayer.complete(null)
//                    return Triple(null, preJoinResult, true)
//                }
//            }
//
//            addPlayer(newPlayer)
//            creatingPlayers.remove(uuid)
//
//            onNetworkConnect(uuid, newPlayer)
//            onServerConnect(uuid, newPlayer, serverUid)
//            creatingPlayer.complete(newPlayer)
//        }
//
//        return Triple(creatingPlayer.await(), PrePlayerJoinTask.Result.ALLOWED, true)
//    }

    protected open suspend fun preJoin(player: P): PrePlayerJoinTask.Result {
        return PrePlayerJoinTask.Result.ALLOWED
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
        val player = playerCache.getIfPresent(uuid)
        if (player != null) {
            val oldProxy = getProxyServerUid(player)
            val oldServer = getServerUid(player)
            if (proxy) {
                removeProxyServer(player, serverUid)
            } else {
                removeServer(player, serverUid)
            }
            onServerDisconnect(uuid, player, serverUid)

            if (!player.connected) {
                playerCache.invalidate(uuid)
                onNetworkDisconnect(uuid, player, oldProxy, oldServer)
            }
        } else {
            log.atWarning()
                .log("Player with UUID $uuid not found during disconnect handling")
            playerCache.invalidate(uuid)
        }
    }

    @MustBeInvokedByOverriders
    open suspend fun onServerConnect(uuid: UUID, player: P, serverUid: Long) {
    }

    @MustBeInvokedByOverriders
    open suspend fun onServerDisconnect(uuid: UUID, player: P, serverUid: Long) {
    }

    @MustBeInvokedByOverriders
    open suspend fun onNetworkDisconnect(uuid: UUID, player: P, oldProxy: Long?, oldServer: Long?) {
        try {
            CloudPlayerDisconnectFromNetworkEvent(this, player).post()
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to publish CloudPlayerDisconnectFromNetworkEvent")
        }
    }

    @MustBeInvokedByOverriders
    open suspend fun onNetworkConnect(uuid: UUID, player: P) {
        try {
            CloudPlayerConnectToNetworkEvent(this, player).post()
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to publish CloudPlayerConnectToNetworkEvent")
        }
    }

    open fun terminate() {}


    private class PreJoinDenied(val result: PrePlayerJoinTask.Result) : RuntimeException() {
        companion object {
            @Serial
            private const val serialVersionUID: Long = -5043277924406776272L
        }
    }
}

val playerManagerImpl get() = CloudPlayerManager.instance as CloudPlayerManagerImpl<out CommonCloudPlayerImpl>

@Component
@Order(CloudLifecycleAware.MISC_PRIORITY)
class CloudPlayerManagerLifecycle : CloudLifecycleAware {
    override suspend fun onDisable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Terminate player manager") {
            playerManagerImpl.terminate()
        }
    }
}
