package dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.UUID

@SurfNettyPacket(DefaultIds.CLIENTBOUND_CLEAR_TITLE_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundClearTitlePacket : NettyPacket {

    companion object {
        val STREAM_CODEC =
            packetCodec(ClientboundClearTitlePacket::write, ::ClientboundClearTitlePacket)
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