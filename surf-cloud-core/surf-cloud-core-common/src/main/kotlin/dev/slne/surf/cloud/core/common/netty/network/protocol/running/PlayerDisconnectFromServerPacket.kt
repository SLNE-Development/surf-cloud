package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonRunningPacketListener
import java.util.*

@SurfNettyPacket(
    DefaultIds.PLAYER_DISCONNECT_FROM_SERVER_PACKET,
    PacketFlow.BIDIRECTIONAL,
    handlerMode = PacketHandlerMode.DEFAULT
)
class PlayerDisconnectFromServerPacket(
    val uuid: UUID,
    val serverName: String,
    val proxy: Boolean
) : NettyPacket(), InternalNettyPacket<CommonRunningPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            PlayerDisconnectFromServerPacket::uuid,
            ByteBufCodecs.STRING_CODEC,
            PlayerDisconnectFromServerPacket::serverName,
            ByteBufCodecs.BOOLEAN_CODEC,
            PlayerDisconnectFromServerPacket::proxy,
            ::PlayerDisconnectFromServerPacket
        )
    }

    override fun handle(listener: CommonRunningPacketListener) {
        listener.handlePlayerDisconnectFromServer(this)
    }
}