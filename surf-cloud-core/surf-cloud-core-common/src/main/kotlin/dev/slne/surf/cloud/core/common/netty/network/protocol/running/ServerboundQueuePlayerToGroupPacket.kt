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
import java.util.*

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_QUEUE_PLAYER_TO_GROUP,
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.DEFAULT
)
data class ServerboundQueuePlayerToGroupPacket(
    val uuid: UUID,
    val group: String,
    val sendQueuedMessage: Boolean
) : RespondingNettyPacket<ClientboundConnectPlayerToServerResponse>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundQueuePlayerToGroupPacket::uuid,
            ByteBufCodecs.STRING_CODEC,
            ServerboundQueuePlayerToGroupPacket::group,
            ByteBufCodecs.BOOLEAN_CODEC,
            ServerboundQueuePlayerToGroupPacket::sendQueuedMessage,
            ::ServerboundQueuePlayerToGroupPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleQueuePlayerToGroup(this)
    }
}