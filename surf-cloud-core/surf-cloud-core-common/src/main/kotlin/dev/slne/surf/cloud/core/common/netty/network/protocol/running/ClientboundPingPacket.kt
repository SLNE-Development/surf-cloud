package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

/**
 * When sent to the client, client responds with a
 * [dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.serverbound.ServerboundPongPacket]
 * packet with the same id.
 */
@SurfNettyPacket(DefaultIds.CLIENTBOUND_PING_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundPingPacket: NettyPacket {

    companion object{
        @JvmStatic
        val STREAM_CODEC = packetCodec(ClientboundPingPacket::write, ::ClientboundPingPacket)
    }

    val pingId: Long

    constructor(pingId: Long) {
        this.pingId = pingId
    }

    private constructor(buffer: SurfByteBuf) {
        pingId = buffer.readLong()
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeLong(pingId)
    }
}