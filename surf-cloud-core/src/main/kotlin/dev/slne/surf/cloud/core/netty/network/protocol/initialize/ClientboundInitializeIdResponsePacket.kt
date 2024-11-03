package dev.slne.surf.cloud.core.netty.network.protocol.initialize

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.packet.packetCodec
import io.netty.buffer.ByteBuf

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_INITIALIZE_ID_RESPONSE,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.INITIALIZE
)
class ClientboundInitializeIdResponsePacket : NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(
            ClientboundInitializeIdResponsePacket::write,
            ::ClientboundInitializeIdResponsePacket
        )
    }

    val generatedId: Long

    constructor(id: Long) {
        this.generatedId = id
    }

    private constructor(buf: ByteBuf) {
        generatedId = buf.readLong()
    }

    private fun write(buf: ByteBuf) {
        buf.writeLong(generatedId)
    }
}