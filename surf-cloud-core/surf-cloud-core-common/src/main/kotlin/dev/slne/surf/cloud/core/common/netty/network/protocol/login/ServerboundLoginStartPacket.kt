package dev.slne.surf.cloud.core.common.netty.network.protocol.login

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket

/**
 * This packet is sent by the client to the server to start the login process.
 */
@SurfNettyPacket(
    DefaultIds.SERVERBOUND_LOGIN_START_PACKET,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.LOGIN,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundLoginStartPacket(
    val serverCategory: String,
    val serverName: String,
    val proxy: Boolean,
    val lobby: Boolean
) : NettyPacket(), InternalNettyPacket<ServerLoginPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_CODEC,
            ServerboundLoginStartPacket::serverCategory,
            ByteBufCodecs.STRING_CODEC,
            ServerboundLoginStartPacket::serverName,
            ByteBufCodecs.BOOLEAN_CODEC,
            ServerboundLoginStartPacket::proxy,
            ByteBufCodecs.BOOLEAN_CODEC,
            ServerboundLoginStartPacket::lobby,
            ::ServerboundLoginStartPacket
        )
    }

    override fun handle(listener: ServerLoginPacketListener) {
        listener.handleLoginStart(this)
    }
}