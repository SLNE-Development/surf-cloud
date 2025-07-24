package dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound.ClientboundPlayerPersistentDataContainerResponse
import java.util.UUID

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_REQUEST_PLAYER_PERSISTENT_DATA_CONTAINER,
    PacketFlow.SERVERBOUND
)
class ServerboundRequestPlayerPersistentDataContainer :
    RespondingNettyPacket<ClientboundPlayerPersistentDataContainerResponse> {
    companion object {
        val STREAM_CODEC = packetCodec(
            ServerboundRequestPlayerPersistentDataContainer::write,
            ::ServerboundRequestPlayerPersistentDataContainer
        )
    }

    val uuid: UUID

    constructor(uuid: UUID) {
        this.uuid = uuid
    }

    private constructor(buf: SurfByteBuf) {
        uuid = buf.readUuid()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
    }
}