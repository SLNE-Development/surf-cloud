package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.util.Either
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import java.util.*

@SurfNettyPacket("cloud:serverbound:pull_players_to_group", PacketFlow.SERVERBOUND)
class ServerboundPullPlayersToGroupPacket(
    val group: String,
    val players: MutableSet<UUID>
) : RespondingNettyPacket<PullPlayersToGroupResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_CODEC,
            ServerboundPullPlayersToGroupPacket::group,
            ByteBufCodecs.UUID_CODEC.apply(ByteBufCodecs.set()),
            ServerboundPullPlayersToGroupPacket::players,
            ::ServerboundPullPlayersToGroupPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handlePullPlayersToGroup(this)
    }
}

@SurfNettyPacket("cloud:response:pull_players_to_group_response", PacketFlow.CLIENTBOUND)
class PullPlayersToGroupResponsePacket(val result: Either<MutableList<Pair<UUID, ConnectionResultEnum>>, String?>) :
    ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = ByteBufCodecs.either(
            ByteBufCodecs
                .pair(ByteBufCodecs.UUID_CODEC, ConnectionResultEnum.STREAM_CODEC)
                .apply(ByteBufCodecs.list()),
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
        ).map(::PullPlayersToGroupResponsePacket, PullPlayersToGroupResponsePacket::result)
    }
}