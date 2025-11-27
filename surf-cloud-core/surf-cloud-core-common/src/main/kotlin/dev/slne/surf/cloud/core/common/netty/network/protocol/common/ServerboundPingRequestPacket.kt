package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.CriticalInternalNettyPacket

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_PING_REQUEST_PACKET,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.RUNNING,
    ConnectionProtocol.PRE_RUNNING,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundPingRequestPacket(val time: Long) : NettyPacket(),
    CriticalInternalNettyPacket<ServerCommonPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG_CODEC,
            ServerboundPingRequestPacket::time,
            ::ServerboundPingRequestPacket
        )
    }

    override fun handle(listener: ServerCommonPacketListener) {
        listener.handlePingRequest(this)
    }
}