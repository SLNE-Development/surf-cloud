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
    "cloud:clientbound:synchronize_finish",
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.DEFAULT
)
@AlwaysImmediate
object ClientboundSynchronizeFinishPacket : NettyPacket(),
    CriticalInternalNettyPacket<ClientSynchronizingPacketListener> {
    val STREAM_CODEC = streamCodecUnitSimple(ClientboundSynchronizeFinishPacket)
    override val terminal: Boolean = true

    override fun handle(listener: ClientSynchronizingPacketListener) {
        listener.handleSynchronizeFinish(this)
    }
}