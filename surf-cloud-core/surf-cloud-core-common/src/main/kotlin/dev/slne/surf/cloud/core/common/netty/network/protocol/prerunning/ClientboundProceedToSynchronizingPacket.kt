package dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_READY_TO_RUN_PACKET,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.PRE_RUNNING
)
object ClientboundProceedToSynchronizingPacket : NettyPacket() {
    val STREAM_CODEC = streamCodecUnitSimple(this)
    override val terminal = true
}