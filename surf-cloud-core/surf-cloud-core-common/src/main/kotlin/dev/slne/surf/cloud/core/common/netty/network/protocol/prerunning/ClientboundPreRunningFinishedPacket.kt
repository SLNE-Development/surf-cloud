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
    DefaultIds.CLIENTBOUND_PRE_RUNNING_FINISHED_PACKET,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.PRE_RUNNING,
    handlerMode = PacketHandlerMode.DEFAULT
)
@AlwaysImmediate
object ClientboundPreRunningFinishedPacket : NettyPacket(),
    CriticalInternalNettyPacket<ClientPreRunningPacketListener> {
    val STREAM_CODEC = streamCodecUnitSimple(ClientboundPreRunningFinishedPacket)

    override fun handle(listener: ClientPreRunningPacketListener) {
        listener.handlePreRunningFinished(this)
    }
}