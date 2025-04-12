package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("cloud:serverbound:pull_players_to_group", PacketFlow.SERVERBOUND)
@Serializable
class ServerboundPullPlayersToGroupPacket(
    val group: String,
    val players: Collection<@Contextual UUID>
) : RespondingNettyPacket<PullPlayersToGroupResponsePacket>()

@SurfNettyPacket("cloud:response:pull_players_to_group_response", PacketFlow.CLIENTBOUND)
@Serializable
class PullPlayersToGroupResponsePacket(val results: List<Pair<@Contextual UUID, ConnectionResultEnum>>) :
    ResponseNettyPacket()