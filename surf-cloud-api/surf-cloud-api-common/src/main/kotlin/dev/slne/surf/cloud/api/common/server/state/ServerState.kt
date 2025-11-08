package dev.slne.surf.cloud.api.common.server.state

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.util.ByIdMap

enum class ServerState(val id: Int, val allowJoin: Boolean) {
    RESTARTING(0, false),
    LOBBY(1, true),
    ONLINE(2, true),
    OFFLINE(3, false);

    companion object {
        val BY_ID = ByIdMap.continuous(
            ServerState::id,
            entries.toTypedArray(),
            ByIdMap.OutOfBoundsStrategy.LAST
        )

        val STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ServerState::id)
    }
}
