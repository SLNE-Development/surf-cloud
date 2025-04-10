package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import java.time.ZonedDateTime

typealias SerializableZonedDateTime = @Serializable(with = ZonedDateTimeSerializer::class) ZonedDateTime

object ZonedDateTimeSerializer : CloudBufSerializer<ZonedDateTime>() {
    override val descriptor = buildClassSerialDescriptor("ZonedDateTime") {
        element<Long>("epochMillis")
        element<String>("zoneId")
    }

    override fun deserialize0(buf: SurfByteBuf): ZonedDateTime {
        return buf.readZonedDateTime()
    }

    override fun serialize0(
        buf: SurfByteBuf,
        value: ZonedDateTime
    ) {
        buf.writeZonedDateTime(value)
    }
}