package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket

@SurfNettyPacket("cloud:serverbound:refresh_whitelist", PacketFlow.SERVERBOUND)
object ServerboundRefreshWhitelistPacket : NettyPacket() {
    val STREAM_CODEC = streamCodecUnitSimple(ServerboundRefreshWhitelistPacket)
}