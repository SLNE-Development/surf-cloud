package dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.serverbound

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_REQUEST_CONTINUATION,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.PRE_RUNNING
)
object ServerboundRequestContinuation : NettyPacket() {
    val STREAM_CODEC = streamCodecUnitSimple(this)
}