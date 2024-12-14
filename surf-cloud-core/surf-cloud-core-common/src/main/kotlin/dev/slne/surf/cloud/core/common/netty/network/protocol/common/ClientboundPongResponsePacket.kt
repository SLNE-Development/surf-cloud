package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(DefaultIds.CLIENTBOUND_PING_REQUEST_RESPONSE_PACKET, PacketFlow.CLIENTBOUND, ConnectionProtocol.RUNNING, ConnectionProtocol.PRE_RUNNING)
class ClientboundPongResponsePacket: NettyPacket {

    companion object {
        val STREAM_CODEC =
            packetCodec(ClientboundPongResponsePacket::write, ::ClientboundPongResponsePacket)
    }

    val time: Long

    constructor(time: Long) {
        this.time = time
    }

    private constructor(buf: SurfByteBuf) {
        time = buf.readLong()
    }

    fun write(buffer: SurfByteBuf) {
        buffer.writeLong(time)
    }
}