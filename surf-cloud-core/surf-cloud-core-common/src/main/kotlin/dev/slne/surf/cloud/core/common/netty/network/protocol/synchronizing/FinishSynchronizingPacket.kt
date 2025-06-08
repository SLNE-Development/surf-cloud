package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket

@dev.slne.surf.cloud.api.common.meta.SurfNettyPacket(
    "cloud:bidirectional:finish_synchronize",
    PacketFlow.BIDIRECTIONAL,
    ConnectionProtocol.SYNCHRONIZING
)
object FinishSynchronizingPacket: NettyPacket() {
    val STREAM_CODEC = streamCodecUnitSimple(FinishSynchronizingPacket)
}