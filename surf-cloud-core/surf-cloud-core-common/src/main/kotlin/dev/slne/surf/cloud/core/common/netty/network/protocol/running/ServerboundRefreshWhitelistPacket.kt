package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket

@SurfNettyPacket(
    "cloud:serverbound:refresh_whitelist",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
object ServerboundRefreshWhitelistPacket : NettyPacket(),
    InternalNettyPacket<RunningServerPacketListener> {
    val STREAM_CODEC = streamCodecUnitSimple(ServerboundRefreshWhitelistPacket)

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleRefreshWhitelist(this)
    }
}