package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.kotlin

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlin.time.Duration

typealias SerializableDuration = @Serializable(with = DurationSerializer::class) Duration

object DurationSerializer : CloudBufSerializer<Duration>() {
    override val descriptor = PrimitiveSerialDescriptor("Duration", PrimitiveKind.LONG)

    override fun serialize0(
        buf: SurfByteBuf,
        value: Duration
    ) {
        buf.writeDuration(value)
    }

    override fun deserialize0(buf: SurfByteBuf): Duration {
        return buf.readDuration()
    }
}