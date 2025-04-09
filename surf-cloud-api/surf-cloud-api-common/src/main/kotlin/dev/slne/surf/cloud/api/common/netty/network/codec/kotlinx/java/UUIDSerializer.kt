package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import org.gradle.internal.impldep.kotlinx.serialization.descriptors.element
import java.util.UUID

typealias SerializableUUID = @Serializable(with = UUIDSerializer::class) UUID

object UUIDSerializer: CloudBufSerializer<UUID>() {
    override val descriptor = buildClassSerialDescriptor("UUID") {
        element<Long>("mostSignificantBits")
        element<Long>("leastSignificantBits")
    }

    override fun serialize0(
        buf: SurfByteBuf,
        value: UUID
    ) {
        buf.writeUuid(value)
    }

    override fun deserialize0(buf: SurfByteBuf): UUID {
        return buf.readUuid()
    }
}