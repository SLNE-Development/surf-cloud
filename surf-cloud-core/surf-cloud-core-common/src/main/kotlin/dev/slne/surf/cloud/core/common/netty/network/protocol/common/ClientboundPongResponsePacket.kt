package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.AlwaysImmediate
import dev.slne.surf.cloud.core.common.netty.network.CriticalInternalNettyPacket

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_PING_REQUEST_RESPONSE_PACKET,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.RUNNING,
    ConnectionProtocol.PRE_RUNNING,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.NETTY
)
@AlwaysImmediate
class ClientboundPongResponsePacket(val time: Long) : NettyPacket(),
    CriticalInternalNettyPacket<ClientCommonPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG_CODEC,
            ClientboundPongResponsePacket::time,
            ::ClientboundPongResponsePacket
        )
    }

    override fun handle(listener: ClientCommonPacketListener) {
        listener.handlePong(this)
    }
}