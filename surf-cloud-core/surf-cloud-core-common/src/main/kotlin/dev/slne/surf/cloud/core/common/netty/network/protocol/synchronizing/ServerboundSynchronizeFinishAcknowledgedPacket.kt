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
    "cloud:serverbound:synchronize_finish_acknowledged",
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.DEFAULT,
)
@AlwaysImmediate
object ServerboundSynchronizeFinishAcknowledgedPacket : NettyPacket(),
    CriticalInternalNettyPacket<ServerSynchronizingPacketListener> {
    val STREAM_CODEC = streamCodecUnitSimple(ServerboundSynchronizeFinishAcknowledgedPacket)
    override val terminal = true

    override fun handle(listener: ServerSynchronizingPacketListener) {
        listener.handleSynchronizeFinishAcknowledged(this)
    }
}