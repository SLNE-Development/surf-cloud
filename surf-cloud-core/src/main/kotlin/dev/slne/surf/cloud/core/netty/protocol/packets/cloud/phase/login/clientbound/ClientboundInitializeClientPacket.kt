package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.clientbound

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.packet.packetCodec
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf

/**
 * Packet sent by the cloud server in response to the [CloudServerboundHandshakePacket] to initialize the client if requested.
 */
@SurfNettyPacket(DefaultIds.CLIENTBOUND_INITIALIZE_CLIENT_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundInitializeClientPacket : NettyPacket {
    companion object {
        @JvmStatic
        val STREAM_CODEC = packetCodec(ClientboundInitializeClientPacket::write, ::ClientboundInitializeClientPacket)
    }

    val serverId: Long
    val serverCategory: String

    constructor(serverId: Long, serverCategory: String) {
        this.serverId = serverId
        this.serverCategory = serverCategory
    }

    private constructor(buf: SurfByteBuf) {
        serverId = buf.readLong()
        serverCategory = buf.readUtf()
    }

    fun write(buffer: SurfByteBuf) {
        buffer.writeLong(serverId)
        buffer.writeUtf(serverCategory)
    }
}