package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_SHUTDOWN_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
object ClientboundTriggerShutdownPacket : NettyPacket(),
    InternalNettyPacket<RunningClientPacketListener> {
    val STREAM_CODEC = streamCodecUnitSimple(this)

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleTriggerShutdown(this)
    }
}