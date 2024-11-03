package dev.slne.surf.cloud.core.netty.network.protocol.running

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.packet.packetCodec
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.netty.network.DisconnectionDetails

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_DISCONNECT_PACKET,
    PacketFlow.CLIENTBOUND
)
class ClientboundDisconnectPacket : NettyPacket {

    companion object {
        val STREAM_CODEC =
            packetCodec(ClientboundDisconnectPacket::write, ::ClientboundDisconnectPacket)
    }

    val details: DisconnectionDetails

    constructor(details: DisconnectionDetails) {
        this.details = details
    }

    constructor(reason: String) {
        this.details = DisconnectionDetails(reason)
    }

    private constructor(buf: SurfByteBuf) {
        details = buf.readJsonWithCodec(DisconnectionDetails.CODEC)
    }

    fun write(buffer: SurfByteBuf) {
        buffer.writeJsonWithCodec(DisconnectionDetails.CODEC, details)
    }
}