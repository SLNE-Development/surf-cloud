package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.nbt.CompoundBinaryTag

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_PLAYER_PERSISTENT_DATA_CONTAINER_RESPONSE,
    PacketFlow.CLIENTBOUND
)
class ClientboundPlayerPersistentDataContainerResponse : ResponseNettyPacket {
    companion object {
        val STREAM_CODEC = packetCodec(
            ClientboundPlayerPersistentDataContainerResponse::write,
            ::ClientboundPlayerPersistentDataContainerResponse
        )
    }

    val verificationId: Int
    val nbt: CompoundBinaryTag

    constructor(verificationId: Int, nbt: CompoundBinaryTag) {
        this.verificationId = verificationId
        this.nbt = nbt
    }

    private constructor(buf: SurfByteBuf) {
        verificationId = buf.readInt()
        nbt = buf.readCompoundTag()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeInt(verificationId)
        buf.writeCompoundTag(nbt)
    }
}