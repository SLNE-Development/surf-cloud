package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket(DefaultIds.SERVERBOUND_CONNECT_PLAYER_TO_SERVER, PacketFlow.SERVERBOUND)
@Serializable
data class ServerboundConnectPlayerToServerPacket(
    val uuid: @Contextual UUID,
    val serverName: String,
    val queue: Boolean,
    val sendQueuedMessage: Boolean = false,
) : RespondingNettyPacket<ClientboundConnectPlayerToServerResponse>()