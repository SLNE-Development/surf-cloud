package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.cache.key.CacheKey
import dev.slne.surf.cloud.api.common.player.cache.key.CacheNetworkKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed interface PlayerCacheDeltaPacket {
    val playerId: UUID
    val key: CacheNetworkKey
    val changeId: Long
}

@SurfNettyPacket("cloud:bidirectional:player_cache/list_delta", PacketFlow.BIDIRECTIONAL)
@Serializable
data class PlayerCacheListDeltaPacket(
    override val playerId: @Contextual UUID,
    override val key: CacheNetworkKey,
    val ops: Array<out ListOp>,
    override val changeId: Long
) : NettyPacket(), PlayerCacheDeltaPacket {

    @Serializable
    sealed interface ListOp {
        @Serializable
        @SerialName("add")
        data class Add(val index: Int?, val element: ByteArray) : ListOp {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Add) return false

                if (index != other.index) return false
                if (!element.contentEquals(other.element)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = index ?: 0
                result = 31 * result + element.contentHashCode()
                return result
            }
        }

        @Serializable
        @SerialName("set")
        data class Set(val index: Int, val element: ByteArray) : ListOp {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Set) return false

                if (index != other.index) return false
                if (!element.contentEquals(other.element)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = index
                result = 31 * result + element.contentHashCode()
                return result
            }
        }

        @Serializable
        @SerialName("remi")
        data class RemoveAt(val index: Int) : ListOp

        @Serializable
        @SerialName("rem")
        data class Remove(val element: ByteArray) : ListOp {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Remove) return false

                if (!element.contentEquals(other.element)) return false

                return true
            }

            override fun hashCode(): Int {
                return element.contentHashCode()
            }
        }

        @Serializable
        @SerialName("clr")
        data object Clear : ListOp
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayerCacheListDeltaPacket) return false

        if (changeId != other.changeId) return false
        if (playerId != other.playerId) return false
        if (key != other.key) return false
        if (!ops.contentEquals(other.ops)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = changeId.hashCode()
        result = 31 * result + playerId.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + ops.contentHashCode()
        return result
    }
}


@SurfNettyPacket("cloud:bidirectional:player_cache/set_delta", PacketFlow.BIDIRECTIONAL)
@Serializable
data class PlayerCacheSetDeltaPacket(
    override val playerId: @Contextual UUID,
    override val key: CacheKey.Set<*>,
    val ops: Array<out SetOp>,
    override val changeId: Long
) : NettyPacket(), PlayerCacheDeltaPacket {
    @Serializable
    sealed interface SetOp {
        @Serializable
        @SerialName("add")
        data class Add(val element: ByteArray) : SetOp {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Add) return false

                if (!element.contentEquals(other.element)) return false

                return true
            }

            override fun hashCode(): Int {
                return element.contentHashCode()
            }
        }

        @Serializable
        @SerialName("rem")
        data class Remove(val element: ByteArray) : SetOp {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Remove) return false

                if (!element.contentEquals(other.element)) return false

                return true
            }

            override fun hashCode(): Int {
                return element.contentHashCode()
            }
        }

        @Serializable
        @SerialName("clr")
        data object Clear : SetOp
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayerCacheSetDeltaPacket) return false
        if (!super.equals(other)) return false

        if (changeId != other.changeId) return false
        if (playerId != other.playerId) return false
        if (key != other.key) return false
        if (!ops.contentEquals(other.ops)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + changeId.hashCode()
        result = 31 * result + playerId.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + ops.contentHashCode()
        return result
    }
}

@SurfNettyPacket("cloud:bidirectional:player_cache/map_delta", PacketFlow.BIDIRECTIONAL)
@Serializable
data class PlayerCacheMapDeltaPacket(
    override val playerId: @Contextual UUID,
    override val key: CacheKey.Map<*, *>,
    val ops: Array<out MapOp>,
    override val changeId: Long
) : NettyPacket(), PlayerCacheDeltaPacket {
    @Serializable
    sealed interface MapOp {
        @Serializable
        @SerialName("put")
        data class Put(val key: ByteArray, val value: ByteArray) : MapOp {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Put) return false

                if (!key.contentEquals(other.key)) return false
                if (!value.contentEquals(other.value)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = key.contentHashCode()
                result = 31 * result + value.contentHashCode()
                return result
            }
        }

        @Serializable
        @SerialName("rem")
        data class Remove(val key: ByteArray) : MapOp {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Remove) return false

                if (!key.contentEquals(other.key)) return false

                return true
            }

            override fun hashCode(): Int {
                return key.contentHashCode()
            }
        }

        @Serializable
        @SerialName("clr")
        data object Clear : MapOp
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayerCacheMapDeltaPacket) return false
        if (!super.equals(other)) return false

        if (changeId != other.changeId) return false
        if (playerId != other.playerId) return false
        if (key != other.key) return false
        if (!ops.contentEquals(other.ops)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + changeId.hashCode()
        result = 31 * result + playerId.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + ops.contentHashCode()
        return result
    }
}