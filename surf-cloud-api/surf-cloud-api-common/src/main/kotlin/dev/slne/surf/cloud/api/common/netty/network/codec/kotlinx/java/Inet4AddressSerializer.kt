package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import java.net.Inet4Address

typealias SerializableInet4Address = @Serializable(with = Inet4AddressSerializer::class) Inet4Address

object Inet4AddressSerializer : CloudBufSerializer<Inet4Address>() {
    override val descriptor = SerialDescriptor("Inet4Address", ByteArraySerializer().descriptor)

    override fun serialize0(
        buf: SurfByteBuf,
        value: Inet4Address
    ) {
        buf.writeInet4Address(value)
    }

    override fun deserialize0(buf: SurfByteBuf): Inet4Address {
        return buf.readInet4Address()
    }
}