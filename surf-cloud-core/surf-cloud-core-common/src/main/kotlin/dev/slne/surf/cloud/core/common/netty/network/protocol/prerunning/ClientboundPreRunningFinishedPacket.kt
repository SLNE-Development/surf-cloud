package dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_PRE_RUNNING_FINISHED_PACKET,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.PRE_RUNNING
)
object ClientboundPreRunningFinishedPacket : NettyPacket() {
    val STREAM_CODEC = streamCodecUnitSimple(ClientboundPreRunningFinishedPacket)
    override val terminal = true
}