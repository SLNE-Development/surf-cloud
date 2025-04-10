package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import java.net.URI

typealias SerializableURI = @Serializable(with = URISerializer::class) URI

object URISerializer : CloudBufSerializer<URI>() {
    override val descriptor = PrimitiveSerialDescriptor("URI", PrimitiveKind.STRING)

    override fun serialize0(
        buf: SurfByteBuf,
        value: URI
    ) {
        buf.writeURI(value)
    }

    override fun deserialize0(buf: SurfByteBuf): URI {
        return buf.readURI()
    }
}