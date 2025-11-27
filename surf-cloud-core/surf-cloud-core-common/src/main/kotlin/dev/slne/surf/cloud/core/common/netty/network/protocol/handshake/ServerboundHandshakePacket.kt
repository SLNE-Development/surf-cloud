package dev.slne.surf.cloud.core.common.netty.network.protocol.handshake

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.AlwaysImmediate
import dev.slne.surf.cloud.core.common.netty.network.CriticalInternalNettyPacket

const val PROTOCOL_VERSION = 1

/**
 * First packet sent by the client to the cloud server. It is used to establish a connection to the cloud server.
 */
@SurfNettyPacket(
    DefaultIds.SERVERBOUND_HANDSHAKE_PACKET,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.HANDSHAKING,
    handlerMode = PacketHandlerMode.DEFAULT
)
@AlwaysImmediate
class ServerboundHandshakePacket(
    val protocolVersion: Int,
    val hostName: String,
    val port: Int,
    val intention: ClientIntent
) : NettyPacket(), CriticalInternalNettyPacket<ServerHandshakePacketListener> {
    override val terminal = true

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT_CODEC,
            ServerboundHandshakePacket::protocolVersion,
            ByteBufCodecs.STRING_CODEC,
            ServerboundHandshakePacket::hostName,
            ByteBufCodecs.VAR_INT_CODEC,
            ServerboundHandshakePacket::port,
            ClientIntent.STREAM_CODEC,
            ServerboundHandshakePacket::intention,
            ::ServerboundHandshakePacket
        )
    }

    override fun handle(listener: ServerHandshakePacketListener) {
        listener.handleHandshake(this)
    }
}