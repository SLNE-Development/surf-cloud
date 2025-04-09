package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor

typealias SerializableUtfString = @Serializable(with = UtfStringSerializer::class) String

object UtfStringSerializer : CloudBufSerializer<String>() {
    override val descriptor = PrimitiveSerialDescriptor("Utf8String", PrimitiveKind.STRING)

    override fun serialize0(buf: SurfByteBuf, value: String) {
        buf.writeUtf(value)
    }

    override fun deserialize0(buf: SurfByteBuf): String {
        return buf.readUtf()
    }
}