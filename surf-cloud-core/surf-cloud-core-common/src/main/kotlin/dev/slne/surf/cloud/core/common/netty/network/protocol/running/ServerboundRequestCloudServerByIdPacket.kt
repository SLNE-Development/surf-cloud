package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(DefaultIds.SERVERBOUND_REQUEST_CLOUD_SERVER_BY_ID, PacketFlow.SERVERBOUND)
class ServerboundRequestCloudServerByIdPacket :
    RespondingNettyPacket<ClientboundResponseCloudServerPacket> {

    companion object {
        val STREAM_CODEC = packetCodec(
            ServerboundRequestCloudServerByIdPacket::write,
            ::ServerboundRequestCloudServerByIdPacket
        )
    }

    val serverId: Long

    constructor(serverId: Long) {
        this.serverId = serverId
    }

    private constructor(buf: SurfByteBuf) {
        serverId = buf.readVarLong()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeVarLong(serverId)
    }
}