package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.clientbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket

@SurfNettyPacket(
    "cloud:clientbound:synchronize_finish",
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.SYNCHRONIZING
)
object ClientboundSynchronizeFinishPacket : NettyPacket() {
    val STREAM_CODEC = streamCodecUnitSimple(ClientboundSynchronizeFinishPacket)
    override val terminal: Boolean = true
}