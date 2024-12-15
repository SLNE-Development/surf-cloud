package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.BundlePacket

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_BUNDLE_PACKET,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.RUNNING, ConnectionProtocol.PRE_RUNNING
)
class ClientboundBundlePacket(subPackets: Iterable<NettyPacket>) : BundlePacket(subPackets)