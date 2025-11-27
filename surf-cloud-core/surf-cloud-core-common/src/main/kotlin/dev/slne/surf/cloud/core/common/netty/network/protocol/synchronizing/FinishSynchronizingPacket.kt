package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.AlwaysImmediate
import dev.slne.surf.cloud.core.common.netty.network.CriticalInternalNettyPacket

@SurfNettyPacket(
    "cloud:bidirectional:finish_synchronize",
    PacketFlow.BIDIRECTIONAL,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.NETTY
)
@AlwaysImmediate
object FinishSynchronizingPacket : NettyPacket(),
    CriticalInternalNettyPacket<ServerSynchronizingPacketListener> {
    val STREAM_CODEC = streamCodecUnitSimple(FinishSynchronizingPacket)

    override fun handle(listener: ServerSynchronizingPacketListener) {
        listener.handleFinishSynchronizing(this)
    }
}