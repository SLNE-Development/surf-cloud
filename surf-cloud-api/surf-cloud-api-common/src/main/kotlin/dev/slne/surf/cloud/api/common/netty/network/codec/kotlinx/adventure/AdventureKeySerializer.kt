package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.adventure

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import net.kyori.adventure.key.Key

typealias SerializableKey = @Serializable(with = AdventureKeySerializer::class) Key

object AdventureKeySerializer : CloudBufSerializer<Key>() {
    override val descriptor = PrimitiveSerialDescriptor("Key", PrimitiveKind.STRING)

    override fun serialize0(
        buf: SurfByteBuf,
        value: Key
    ) {
        buf.writeKey(value)
    }

    override fun deserialize0(buf: SurfByteBuf): Key {
        return buf.readKey()
    }
}