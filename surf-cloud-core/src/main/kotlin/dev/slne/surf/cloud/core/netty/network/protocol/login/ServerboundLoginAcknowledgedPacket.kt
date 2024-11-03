package dev.slne.surf.cloud.core.netty.network.protocol.login

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.network.codec.streamCodecUnit
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import io.netty.buffer.ByteBuf

/**
 * This packet is sent by the client to the server to acknowledge the login process.
 * When the server receives this packet, it will mark the client as connected.
 */
@SurfNettyPacket(DefaultIds.SERVERBOUND_LOGIN_ACKNOWLEDGED_PACKET, PacketFlow.SERVERBOUND, ConnectionProtocol.LOGIN)
object ServerboundLoginAcknowledgedPacket : NettyPacket() {
    @JvmStatic
    val STREAM_CODEC = streamCodecUnit<ByteBuf, ServerboundLoginAcknowledgedPacket>(this)

    override val terminal = true
}