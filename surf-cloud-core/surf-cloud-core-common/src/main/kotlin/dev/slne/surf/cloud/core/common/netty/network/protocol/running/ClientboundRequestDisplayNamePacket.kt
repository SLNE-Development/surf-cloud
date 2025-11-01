package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.*

@SurfNettyPacket(DefaultIds.CLIENTBOUND_REQUEST_DISPLAY_NAME_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundRequestDisplayNamePacket :
    RespondingNettyPacket<ResponseDisplayNamePacketRequestPacket> {

    companion object {
        val STREAM_CODEC = packetCodec(
            ClientboundRequestDisplayNamePacket::write,
            ::ClientboundRequestDisplayNamePacket
        )
    }

    val uuid: UUID

    constructor(uuid: UUID) {
        this.uuid = uuid
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
    }
}