package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

/**
 * The server will frequently send out a keep-alive (see
 * [dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.clientbound.ClientboundKeepAlivePacket]),
 * each containing a random ID. The client must respond with the same packet.
 */
@SurfNettyPacket(
    DefaultIds.SERVERBOUND_KEEP_ALIVE_PACKET, PacketFlow.SERVERBOUND,
    ConnectionProtocol.RUNNING, ConnectionProtocol.PRE_RUNNING
)
class ServerboundKeepAlivePacket : NettyPacket {
    companion object {
        @JvmStatic
        val STREAM_CODEC =
            packetCodec(ServerboundKeepAlivePacket::write, ::ServerboundKeepAlivePacket)
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