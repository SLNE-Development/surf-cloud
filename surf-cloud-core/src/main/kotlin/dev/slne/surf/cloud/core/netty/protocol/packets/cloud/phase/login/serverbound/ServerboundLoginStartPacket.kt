package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.serverbound

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.packet.packetCodec
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf

/**
 * This packet is sent by the client to the server to start the login process.
 */
@SurfNettyPacket(DefaultIds.SERVERBOUND_LOGIN_START_PACKET, PacketFlow.SERVERBOUND, ConnectionProtocol.LOGIN) // aka HelloPacket
class ServerboundLoginStartPacket : NettyPacket {

    companion object {
        @JvmStatic
        val STREAM_CODEC =
            packetCodec(ServerboundLoginStartPacket::write, ::ServerboundLoginStartPacket)
    }

    val serverId: Long
    val serverCategory: String

    constructor(serverId: Long, serverCategory: String) {
        this.serverId = serverId
        this.serverCategory = serverCategory
    }

    private constructor(buffer: SurfByteBuf) {
        serverId = buffer.readLong()
        serverCategory = buffer.readUtf()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeLong(serverId)
        buf.writeUtf(serverCategory)
    }
}