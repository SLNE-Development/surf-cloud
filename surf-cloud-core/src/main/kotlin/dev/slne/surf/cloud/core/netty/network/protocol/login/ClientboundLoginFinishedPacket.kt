package dev.slne.surf.cloud.core.netty.network.protocol.login

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.network.codec.streamCodecUnit
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import io.netty.buffer.ByteBuf

/**
 * This packet is sent by the server to the client to indicate that the login process was successful on the server.
 */
@SurfNettyPacket(DefaultIds.CLIENTBOUND_LOGIN_FINISHED_PACKET, PacketFlow.CLIENTBOUND, ConnectionProtocol.LOGIN)
object ClientboundLoginFinishedPacket : NettyPacket() {
    @JvmStatic
    val STREAM_CODEC =
        streamCodecUnit<ByteBuf, ClientboundLoginFinishedPacket>(ClientboundLoginFinishedPacket)

    override val terminal = true
}