package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor

typealias LongAsVarLong = @Serializable(with = VarLongSerializer::class) Long

object VarLongSerializer : CloudBufSerializer<Long>() {
    override val descriptor = PrimitiveSerialDescriptor("VarLong", PrimitiveKind.LONG)

    override fun serialize0(buf: SurfByteBuf, value: Long) {
        buf.writeVarLong(value)
    }

    override fun deserialize0(buf: SurfByteBuf): Long {
        return buf.readVarLong()
    }
}