package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.cloud

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element

typealias SerializableOfflineCloudPlayer = @Serializable(with = OfflineCloudPlayerSerializer::class) OfflineCloudPlayer

object OfflineCloudPlayerSerializer : CloudBufSerializer<OfflineCloudPlayer>() {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("OfflineCloudPlayer") {
        element<Long>("uuidMost")
        element<Long>("uuidLeast")
    }

    override fun serialize0(
        buf: SurfByteBuf,
        value: OfflineCloudPlayer
    ) {
        buf.writeUuid(value.uuid)
    }

    override fun deserialize0(buf: SurfByteBuf): OfflineCloudPlayer {
        return buf.readUuid().toOfflineCloudPlayer()
    }
}