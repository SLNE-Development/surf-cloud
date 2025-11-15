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
    DefaultIds.CLIENTBOUND_ADD_PLAYER_TO_SERVER,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundAddPlayerToServerPacket(
    val serverName: String,
    val playerUuid: UUID
) : NettyPacket(), InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_CODEC,
            ClientboundAddPlayerToServerPacket::serverName,
            ByteBufCodecs.UUID_CODEC,
            ClientboundAddPlayerToServerPacket::playerUuid,
            ::ClientboundAddPlayerToServerPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleAddPlayerToServer(this)
    }
}