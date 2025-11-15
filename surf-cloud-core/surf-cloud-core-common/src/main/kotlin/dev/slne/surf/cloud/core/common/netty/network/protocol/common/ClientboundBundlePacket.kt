package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.BundlePacket

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_BUNDLE_PACKET,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.RUNNING, ConnectionProtocol.PRE_RUNNING, ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundBundlePacket(subPackets: Iterable<NettyPacket>) : BundlePacket(subPackets),
    InternalNettyPacket<ClientCommonPacketListener> {
    override fun handle(listener: ClientCommonPacketListener) {
        listener.handleBundlePacket(this)
    }
}