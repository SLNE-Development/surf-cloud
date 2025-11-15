package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import kotlinx.serialization.Contextual
import java.util.*

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_CONNECT_PLAYER_TO_SERVER,
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.DEFAULT
)
data class ServerboundConnectPlayerToServerPacket(
    val uuid: @Contextual UUID,
    val serverName: String,
    val queue: Boolean,
    val sendQueuedMessage: Boolean = false,
) : RespondingNettyPacket<ClientboundConnectPlayerToServerResponse>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundConnectPlayerToServerPacket::uuid,
            ByteBufCodecs.STRING_CODEC,
            ServerboundConnectPlayerToServerPacket::serverName,
            ByteBufCodecs.BOOLEAN_CODEC,
            ServerboundConnectPlayerToServerPacket::queue,
            ByteBufCodecs.BOOLEAN_CODEC,
            ServerboundConnectPlayerToServerPacket::sendQueuedMessage,
            ::ServerboundConnectPlayerToServerPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleConnectPlayerToServer(this)
    }
}