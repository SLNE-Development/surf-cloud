package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket

@SurfNettyPacket(
    "cloud:serverbound:synchronize_finish_acknowledged",
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.SYNCHRONIZING
)
object ServerboundSynchronizeFinishAcknowledgedPacket : NettyPacket() {
    val STREAM_CODEC = streamCodecUnitSimple(ServerboundSynchronizeFinishAcknowledgedPacket)
    override val terminal = true
}