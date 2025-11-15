package dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import java.net.InetSocketAddress

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_READY_TO_RUN_PACKET,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.PRE_RUNNING,
    handlerMode = PacketHandlerMode.DEFAULT
)
class ServerboundProceedToSynchronizingAcknowledgedPacket(
    val playAddress: InetSocketAddress
) : NettyPacket(), InternalNettyPacket<ServerPreRunningPacketListener> {
    override val terminal = true

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INET_SOCKET_ADDRESS_CODEC,
            ServerboundProceedToSynchronizingAcknowledgedPacket::playAddress,
            ::ServerboundProceedToSynchronizingAcknowledgedPacket
        )
    }

    override fun handle(listener: ServerPreRunningPacketListener) {
        listener.handleReadyToRun(this)
    }
}