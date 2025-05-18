package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.cloud

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element

typealias SerializableCloudPlayer = @Serializable(with = CloudPlayerSerializer::class) CloudPlayer

object CloudPlayerSerializer : CloudBufSerializer<CloudPlayer>() {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CloudPlayer") {
        element<Long>("uuidMost")
        element<Long>("uuidLeast")
    }

    override fun serialize0(
        buf: SurfByteBuf,
        value: CloudPlayer
    ) {
        buf.writeUuid(value.uuid)
    }

    override fun deserialize0(buf: SurfByteBuf): CloudPlayer {
        val uuid = buf.readUuid()
        return uuid.toCloudPlayer()
            ?: error("Cannot deserialize CloudPlayer $uuid to CloudPlayer because the player is not online")
    }
}