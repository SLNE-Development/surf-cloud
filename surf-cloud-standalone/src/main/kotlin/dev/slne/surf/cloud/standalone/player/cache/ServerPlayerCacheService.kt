package dev.slne.surf.cloud.standalone.player.cache

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterAccess
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import dev.slne.surf.cloud.api.common.player.cache.CloudPlayerCache
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerCacheFullSyncPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerCacheRequestFullSyncPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerCacheValueUpdatePacket
import dev.slne.surf.cloud.core.common.player.cache.CacheWire
import dev.slne.surf.cloud.core.common.player.cache.ChangeCounter
import dev.slne.surf.cloud.core.common.player.cache.CloudPlayerCacheImpl
import dev.slne.surf.cloud.core.common.player.cache.snapshot.PlayerCacheEntrySnapshot
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.hours

@Component
class ServerPlayerCacheService(
    private val wireFactory: (UUID) -> CacheWire
) {
    private val counters = ConcurrentHashMap<UUID, ChangeCounter>()

    private val caches = Caffeine.newBuilder()
        .expireAfterAccess(1.hours)
        .build<UUID, CloudPlayerCacheImpl> { playerId ->
            CloudPlayerCacheImpl(
                playerId,
                wireFactory(playerId),
                counters.computeIfAbsent(playerId) { ChangeCounter() }
            )
        }

    fun cache(playerId: UUID): CloudPlayerCache = caches.get(playerId)

    @SurfNettyPacketHandler
    fun onRequestFullSync(packet: PlayerCacheRequestFullSyncPacket, info: NettyPacketInfo) {
        val uuid = packet.uuid
        val cache = caches.get(uuid)
        val snapshots = buildSnapshots(cache)
        val response = PlayerCacheFullSyncPacket(
            uuid,
            counters[uuid]?.current() ?: 0,
            snapshots
        )
        info.origin.send(response)
    }

    @SurfNettyPacketHandler
    fun onValueUpdate(packet: PlayerCacheValueUpdatePacket, info: NettyPacketInfo) {

    }

    private fun buildSnapshots(c: CloudPlayerCacheImpl): List<PlayerCacheEntrySnapshot> {
        // Optional: vollständiger Snapshot für FullSync
        return emptyList()
    }
}