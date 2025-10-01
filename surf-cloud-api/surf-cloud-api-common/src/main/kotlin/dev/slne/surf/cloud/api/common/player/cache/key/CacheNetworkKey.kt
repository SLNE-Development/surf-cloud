package dev.slne.surf.cloud.api.common.player.cache.key

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecComposite
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.key.Key

@Serializable(with = CacheNetworkKeySerializer::class)
interface CacheNetworkKey : Key {
    companion object {
        val STREAM_CODEC =
            streamCodecComposite(ByteBufCodecs.KEY_CODEC, CacheNetworkKey::key, ::wrap)

        private fun wrap(key: Key): CacheNetworkKey = CacheNetworkKeyImpl(key)
        fun wrap(key: CacheKey<*>): CacheNetworkKey = CacheNetworkKeyImpl(key)
    }
}

internal class CacheNetworkKeyImpl(val key: Key) : CacheNetworkKey {
    override fun namespace() = key.namespace()
    override fun value() = key.value()
    override fun asString() = key.asString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CacheNetworkKeyImpl) return false

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}

internal object CacheNetworkKeySerializer : KSerializer<CacheNetworkKey> {
    override val descriptor = PrimitiveSerialDescriptor("CacheNetworkKey", PrimitiveKind.STRING)
    override fun serialize(
        encoder: Encoder,
        value: CacheNetworkKey
    ) {
        encoder.encodeString(value.asString())
    }

    override fun deserialize(decoder: Decoder): CacheNetworkKey {
        val str = decoder.decodeString()
        val key = Key.key(str)
        return CacheNetworkKeyImpl(key)
    }
}