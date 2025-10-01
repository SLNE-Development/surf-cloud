package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.cache.key.CacheNetworkKey
import kotlinx.serialization.Serializable

/**
 * Teil-Paket einer Player-Cache-Hydration. Enthält eine oder mehrere Cache-Entries.
 * Einträge sind kodiert als: [keyId][version][size][valueBytes] je Eintrag.
 */
@SurfNettyPacket(
    "cloud:clientbound:player_cache/hydrate_chunk",
    PacketFlow.CLIENTBOUND
)
@Serializable
class ClientboundPlayerCacheHydrateChunkPacket(val entries: List<Entry>): NettyPacket() {
    @Serializable
    data class Entry(val key: CacheNetworkKey, val version: Long, val valueBytes: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Entry) return false

            if (version != other.version) return false
            if (key != other.key) return false
            if (!valueBytes.contentEquals(other.valueBytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = version.hashCode()
            result = 31 * result + key.hashCode()
            result = 31 * result + valueBytes.contentHashCode()
            return result
        }
    }
}