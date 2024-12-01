package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(DefaultIds.CLIENTBOUND_UNREGISTER_SERVER_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundUnregisterServerPacket: NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(ClientboundUnregisterServerPacket::write, ::ClientboundUnregisterServerPacket)
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