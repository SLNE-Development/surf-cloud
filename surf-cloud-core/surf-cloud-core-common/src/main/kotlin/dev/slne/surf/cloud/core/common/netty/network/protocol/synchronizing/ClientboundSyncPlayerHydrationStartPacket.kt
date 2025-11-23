package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket

@SurfNettyPacket(
    "cloud:clientbound:sync_players/start",
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.NETTY
)
object ClientboundSyncPlayerHydrationStartPacket : NettyPacket(), InternalNettyPacket<ClientSynchronizingPacketListener> {
    val STREAM_CODEC = streamCodecUnitSimple(this)
    override fun handle(listener: ClientSynchronizingPacketListener) {
        listener.handleSyncPlayerHydrationStart(this)
    }
}