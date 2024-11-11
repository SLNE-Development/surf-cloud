package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.core.common.netty.network.protocol.BundleDelimiterPacket

@SurfNettyPacket(DefaultIds.CLIENTBOUND_BUNDLE_DELIMITER_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundBundleDelimiterPacket: BundleDelimiterPacket()