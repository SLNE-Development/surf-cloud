package dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.AlwaysImmediate
import dev.slne.surf.cloud.core.common.netty.network.CriticalInternalNettyPacket

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_REQUEST_CONTINUATION,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.PRE_RUNNING,
    handlerMode = PacketHandlerMode.NETTY
)
@AlwaysImmediate
object ServerboundRequestContinuation : NettyPacket(),
    CriticalInternalNettyPacket<ServerPreRunningPacketListener> {
    val STREAM_CODEC = streamCodecUnitSimple(this)

    override fun handle(listener: ServerPreRunningPacketListener) {
        listener.handleRequestContinuation(this)
    }
}