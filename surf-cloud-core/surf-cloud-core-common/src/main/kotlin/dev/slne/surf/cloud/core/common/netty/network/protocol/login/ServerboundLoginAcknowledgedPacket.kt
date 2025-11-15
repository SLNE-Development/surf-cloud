package dev.slne.surf.cloud.core.common.netty.network.protocol.login

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket

/**
 * This packet is sent by the client to the server to acknowledge the login process.
 * When the server receives this packet, it will mark the client as connected.
 */
@SurfNettyPacket(
    DefaultIds.SERVERBOUND_LOGIN_ACKNOWLEDGED_PACKET,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.LOGIN,
    handlerMode = PacketHandlerMode.DEFAULT
)
object ServerboundLoginAcknowledgedPacket : NettyPacket(), InternalNettyPacket<ServerLoginPacketListener> {
    val STREAM_CODEC = streamCodecUnitSimple(this)
    override val terminal = true

    override fun handle(listener: ServerLoginPacketListener) {
        listener.handleLoginAcknowledgement(this)
    }
}