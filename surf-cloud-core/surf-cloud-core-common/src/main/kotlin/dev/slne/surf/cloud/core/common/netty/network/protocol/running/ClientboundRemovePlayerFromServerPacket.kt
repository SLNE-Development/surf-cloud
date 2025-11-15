package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import java.util.*

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_REMOVE_PLAYER_FROM_SERVER,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundRemovePlayerFromServerPacket(
    val serverName: String,
    val playerUuid: UUID
) : NettyPacket(), InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_CODEC,
            ClientboundRemovePlayerFromServerPacket::serverName,
            ByteBufCodecs.UUID_CODEC,
            ClientboundRemovePlayerFromServerPacket::playerUuid,
            ::ClientboundRemovePlayerFromServerPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleRemovePlayerFromServer(this)
    }
}