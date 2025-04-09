package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor

typealias IntAsVarInt = @Serializable(with = VarIntSerializer::class) Int

object VarIntSerializer : CloudBufSerializer<Int>() {
    override val descriptor = PrimitiveSerialDescriptor("VarInt", PrimitiveKind.INT)

    override fun serialize0(buf: SurfByteBuf, value: Int) {
        buf.writeVarInt(value)
    }

    override fun deserialize0(buf: SurfByteBuf): Int {
        return buf.readVarInt()
    }
}