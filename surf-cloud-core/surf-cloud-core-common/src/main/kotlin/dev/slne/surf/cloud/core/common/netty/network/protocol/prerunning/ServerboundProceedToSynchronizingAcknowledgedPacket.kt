package dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.net.InetSocketAddress

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_READY_TO_RUN_PACKET,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.PRE_RUNNING
)
@Serializable
class ServerboundProceedToSynchronizingAcknowledgedPacket(val playAddress: @Contextual InetSocketAddress) :
    NettyPacket() {
    override val terminal = true
}