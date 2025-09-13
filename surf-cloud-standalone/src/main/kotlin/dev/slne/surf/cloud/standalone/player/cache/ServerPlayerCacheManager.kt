package dev.slne.surf.cloud.standalone.player.cache

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterAccess
import com.sksamuel.aedile.core.withRemovalListener
import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.player.cache.key.CacheKey
import dev.slne.surf.cloud.api.common.player.cache.key.CacheNetworkKey
import dev.slne.surf.cloud.api.common.player.cache.key.PlayerCacheKeyRegistry
import dev.slne.surf.cloud.core.common.coroutines.PlayerCacheLoadScope
import dev.slne.surf.cloud.core.common.coroutines.PlayerCacheSaveScope
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.player.cache.CacheOperationResult
import dev.slne.surf.cloud.core.common.player.cache.OperationCoders
import dev.slne.surf.cloud.standalone.config.StandaloneConfigHolder
import kotlinx.coroutines.sync.Mutex
import org.springframework.stereotype.Component
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.minutes

@Component
class ServerPlayerCacheManager(
    private val storageAdapter: PlayerCacheStorageAdapterRegistry,
    private val keyRegistry: PlayerCacheKeyRegistry,
    private val configHolder: StandaloneConfigHolder
) {
    private val cache = Caffeine.newBuilder()
        .expireAfterAccess(30.minutes) // TODO: 31.08.2025 10:58 - add config option
        .withRemovalListener(PlayerCacheSaveScope) { uuid, cache, cause ->
            if (uuid is UUID && cache is ServerPlayerCache) {
                storageAdapter.savePlayerCache(uuid)
            }
        }
        .asLoadingCache<UUID, ServerPlayerCache>(PlayerCacheLoadScope) { uuid ->
            ServerPlayerCache(uuid)
        }

    private val subscribers = Caffeine.newBuilder()
        .build<UUID, MutableSet<Connection>>()

    suspend fun getOrLoadCache(uuid: UUID): ServerPlayerCache {
        return cache.get(uuid)
    }

    suspend fun applyOperation(
        uuid: UUID,
        key: CacheNetworkKey,
        expectedVersion: Long,
        kind: ServerboundCacheOpPacket.Kind,
        valueBytes: ByteArray,
        sourceConnection: Connection
    ) {
        val cacheKey = keyRegistry.byNetworkKey(key)
        if (cacheKey == null) {
            val packet = ClientboundCacheErrorPacket(
                uuid,
                key,
                ClientboundCacheErrorPacket.ErrorCode.UNKNOWN_KEY
            )
            sourceConnection.send(packet)
            return
        }

        val op = when (cacheKey) {
            is CacheKey.Value -> OperationCoders.decodeValueOp(cacheKey, kind, valueBytes)
            is CacheKey.List<*> -> OperationCoders.decodeListOp(cacheKey, kind, valueBytes)
            is CacheKey.Map<*, *> -> OperationCoders.decodeMapOp(cacheKey, kind, valueBytes)
            is CacheKey.Set<*> -> OperationCoders.decodeSetOp(cacheKey, kind, valueBytes)
            is CacheKey.Structured<*, *> -> OperationCoders.decodeStructuredOp(
                cacheKey,
                kind,
                valueBytes
            )
        }

        val playerCache = getOrLoadCache(uuid)
        val result = playerCache.applyOperationInternal(cacheKey, expectedVersion, op)
        when (result) {
            is CacheOperationResult.Success -> {
                broadcastDelta(uuid, key, result.newVersion, kind, valueBytes)
            }

            CacheOperationResult.VersionMismatch -> {
                val packet = ClientboundCacheErrorPacket(
                    uuid,
                    key,
                    ClientboundCacheErrorPacket.ErrorCode.VERSION_CONFLICT
                )
                sourceConnection.send(packet)
            }
        }
    }

    private fun broadcastDelta(
        uuid: UUID,
        key: CacheNetworkKey,
        newVersion: Long,
        kind: ServerboundCacheOpPacket.Kind,
        payload: ByteArray
    ) {
        val subscribers = subscribers.getIfPresent(uuid) ?: return
        val packet = ClientboundCacheDeltaPacket(
            uuid,
            key,
            newVersion,
            kind,
            payload
        )
        for (conn in subscribers) {
            conn.send(packet)
        }
    }

    fun updateWatchList(connection: Connection, newWatched: Set<UUID>) {
        // invalidate stale entries
        val stale = subscribers.asMap().filterValues { connection in it }.keys - newWatched
        for (uuid in stale) {
            val currentSubscribers = subscribers.getIfPresent(uuid) ?: continue
            currentSubscribers.remove(connection)
            if (currentSubscribers.isEmpty()) {
                subscribers.invalidate(uuid)
            }
        }

        for (uuid in newWatched) {
            val connections = subscribers.get(uuid) { Collections.newSetFromMap(WeakHashMap()) }
            if (connections.add()) {

            }
        }
    }

    fun sendHydration(uuid: UUID, cache: ServerPlayerCache, connection: Connection) {
        connection.send(ClientboundPlayerCacheHydrateStartPacket(uuid))
        val data = cache.exportData()
        val chunkSize = configHolder.config.playerCache.hydrationChunkSize
        var idx = 0
        while (idx < data.size) {
            val sublist = data.subList(idx, min(idx + chunkSize, data.size))
            val entries = sublist.map { (key, entry) ->
                ClientboundPlayerCacheHydrateChunkPacket.Entry(
                    key,
                    entry.version(),
                    entry.valueBytes(keyRegistry)
                )
            }
            ClientboundPlayerCacheHydrateChunkPacket(entries)
            idx += chunkSize
        }
        connection.send(ClientboundPlayerCacheHydrateEndPacket(uuid))
    }

    class StripedMutex(stripes: Int = 64) {
        private val size = max(1, stripes).takeHighestOneBit().coerceAtLeast(1)
        private val arr = Array(size) { Mutex() }
        private fun index(playerId: UUID, key: CacheNetworkKey): Int {
            val h1 = playerId.hashCode()
            val h2 = key.hashCode()
            val h = h1 * 31 + h2
            return h and (size - 1)
        }

        fun forKey(playerId: UUID, key: CacheNetworkKey): Mutex = arr[index(playerId, key)]
    }
}