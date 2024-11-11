package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.BundlePacket

@SurfNettyPacket(DefaultIds.SERVERBOUND_BUNDLE_PACKET, PacketFlow.SERVERBOUND)
class ServerboundBundlePacket(subPackets: Iterable<NettyPacket>): BundlePacket(subPackets) {
}