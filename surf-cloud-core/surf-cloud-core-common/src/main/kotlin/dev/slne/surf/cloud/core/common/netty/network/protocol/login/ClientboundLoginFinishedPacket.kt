package dev.slne.surf.cloud.core.common.netty.network.protocol.login

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.AlwaysImmediate
import dev.slne.surf.cloud.core.common.netty.network.CriticalInternalNettyPacket

/**
 * This packet is sent by the server to the client to indicate that the login process was successful on the server.
 */
@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_LOGIN_FINISHED_PACKET,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.LOGIN,
    handlerMode = PacketHandlerMode.DEFAULT
)
@AlwaysImmediate
object ClientboundLoginFinishedPacket : NettyPacket(),
    CriticalInternalNettyPacket<ClientLoginPacketListener> {
    val STREAM_CODEC = streamCodecUnitSimple(this)
    override val terminal = true

    override fun handle(listener: ClientLoginPacketListener) {
        listener.handleLoginFinished(this)
    }
}