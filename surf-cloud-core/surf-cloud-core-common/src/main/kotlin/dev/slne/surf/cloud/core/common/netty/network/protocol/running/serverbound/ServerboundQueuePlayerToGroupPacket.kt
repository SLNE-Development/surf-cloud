package dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound.ClientboundConnectPlayerToServerResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SurfNettyPacket(DefaultIds.SERVERBOUND_QUEUE_PLAYER_TO_GROUP, PacketFlow.SERVERBOUND)
data class ServerboundQueuePlayerToGroupPacket(
    val uuid: @Contextual UUID,
    val group: String,
    val sendQueuedMessage: Boolean
) : RespondingNettyPacket<ClientboundConnectPlayerToServerResponse>()