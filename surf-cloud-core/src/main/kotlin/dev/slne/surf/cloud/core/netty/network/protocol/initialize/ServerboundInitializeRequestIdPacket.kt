package dev.slne.surf.cloud.core.netty.network.protocol.initialize

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_INITIALIZE_REQUEST_ID_PACKET,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.INITIALIZE
)
object ServerboundInitializeRequestIdPacket : NettyPacket() {
    @JvmStatic
    val STREAM_CODEC = streamCodecUnitSimple(ServerboundInitializeRequestIdPacket)
}