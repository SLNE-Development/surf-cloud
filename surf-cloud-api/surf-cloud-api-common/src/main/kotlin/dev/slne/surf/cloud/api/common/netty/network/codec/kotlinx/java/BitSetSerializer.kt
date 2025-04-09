package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import java.util.*

typealias SerializableBitSet = @Serializable(with = BitSetSerializer::class) BitSet

object BitSetSerializer : CloudBufSerializer<BitSet>() {
    override val descriptor = buildClassSerialDescriptor("BitSet") {
        element<LongArray>("longArray")
    }

    override fun serialize0(
        buf: SurfByteBuf,
        value: BitSet
    ) {
        buf.writeBitSet(value)
    }

    override fun deserialize0(buf: SurfByteBuf): BitSet {
        return buf.readBitSet()
    }
}