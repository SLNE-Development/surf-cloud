package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

/**
 * The server will frequently send out a keep-alive, each containing a random ID. The client must
 * respond with the same payload (see
 * [ServerboundKeepAlivePacket]).
 * If the client does not respond to a
 * Keep Alive packet within `15 seconds` after it was sent, the server kicks the client. Vice versa,
 * if the server does not send any keep-alives for `20 seconds`, the client will disconnect and yields
 * a "Timed out" exception.
 */
@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_KEEP_ALIVE_PACKET, PacketFlow.CLIENTBOUND,
    ConnectionProtocol.RUNNING, ConnectionProtocol.PRE_RUNNING
)
class ClientboundKeepAlivePacket : NettyPacket {
    companion object {
        @JvmStatic
        val STREAM_CODEC =
            packetCodec(ClientboundKeepAlivePacket::write, ::ClientboundKeepAlivePacket)
    }

    val keepAliveId: Long

    constructor(keepAliveId: Long) {
        this.keepAliveId = keepAliveId
    }

    private constructor(buffer: SurfByteBuf) {
        keepAliveId = buffer.readLong()
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeLong(keepAliveId)
    }
}