package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.netty.network.DisconnectReason
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails

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

    constructor(reason: DisconnectReason) {
        this.details = DisconnectionDetails(reason)
    }

    private constructor(buf: SurfByteBuf) {
        details = buf.readJsonWithCodec(DisconnectionDetails.CODEC)
    }

    fun write(buffer: SurfByteBuf) {
        buffer.writeJsonWithCodec(DisconnectionDetails.CODEC, details)
    }
}