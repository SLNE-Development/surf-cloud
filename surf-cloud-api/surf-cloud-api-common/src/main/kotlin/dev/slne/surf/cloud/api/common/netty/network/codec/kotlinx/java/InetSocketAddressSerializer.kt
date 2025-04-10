package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import java.net.InetSocketAddress

typealias SerializableInetSocketAddress = @Serializable(with = InetSocketAddressSerializer::class) InetSocketAddress

object InetSocketAddressSerializer : CloudBufSerializer<InetSocketAddress>() {
    override val descriptor = buildClassSerialDescriptor("InetSocketAddress") {
        element<String>("host")
        element<Int>("port")
    }

    override fun serialize0(
        buf: SurfByteBuf,
        value: InetSocketAddress
    ) {
        buf.writeInetSocketAddress(value)
    }

    override fun deserialize0(buf: SurfByteBuf): InetSocketAddress {
        return buf.readInetSocketAddress()
    }
}