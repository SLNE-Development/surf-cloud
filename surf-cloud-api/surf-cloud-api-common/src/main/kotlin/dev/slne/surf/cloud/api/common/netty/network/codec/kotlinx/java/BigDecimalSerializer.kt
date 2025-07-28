package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import java.math.BigDecimal

typealias SerializableBigDecimal = @Serializable(with = BigDecimalSerializer::class) BigDecimal

object BigDecimalSerializer : CloudBufSerializer<BigDecimal>() {
    override val descriptor = buildClassSerialDescriptor("dev.slne.surf.cloud.BigDecimal") {
        element<ByteArray>("unscaledValue")
        element<Int>("scale")
        element<Int>("precision")
    }

    override fun serialize0(buf: SurfByteBuf, value: BigDecimal) {
        buf.writeBigDecimal(value)
    }

    override fun deserialize0(buf: SurfByteBuf): BigDecimal {
        return buf.readBigDecimal()
    }
}