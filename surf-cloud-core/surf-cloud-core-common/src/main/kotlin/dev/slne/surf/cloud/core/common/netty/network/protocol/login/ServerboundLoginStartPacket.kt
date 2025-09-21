package dev.slne.surf.cloud.core.common.netty.network.protocol.login

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

/**
 * This packet is sent by the client to the server to start the login process.
 */
@SurfNettyPacket(
    DefaultIds.SERVERBOUND_LOGIN_START_PACKET,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.LOGIN
) // aka HelloPacket
class ServerboundLoginStartPacket : NettyPacket {

    companion object {
        @JvmStatic
        val STREAM_CODEC =
            packetCodec(ServerboundLoginStartPacket::write, ::ServerboundLoginStartPacket)
    }

    val serverCategory: String
    val serverName: String
    val proxy: Boolean
    val lobby: Boolean

    constructor(
        serverCategory: String,
        serverName: String,
        proxy: Boolean,
        lobby: Boolean,
    ) {
        this.serverCategory = serverCategory
        this.serverName = serverName
        this.proxy = proxy
        this.lobby = lobby
    }

    private constructor(buffer: SurfByteBuf) {
        serverCategory = buffer.readUtf()
        serverName = buffer.readUtf()
        proxy = buffer.readBoolean()
        lobby = buffer.readBoolean()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUtf(serverCategory)
        buf.writeUtf(serverName)
        buf.writeBoolean(proxy)
        buf.writeBoolean(lobby)
    }
}