package dev.slne.surf.cloud.core.common.netty.network.protocol.handshake

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.util.ByIdMap

enum class ClientIntent(val id: Int) {
    STATUS(1), // Currently not in use
    INITIALIZE(2),
    LOGIN(3);

    companion object {
        val BY_ID = ByIdMap.continuous(
            ClientIntent::id,
            entries.toTypedArray(),
            ByIdMap.OutOfBoundsStrategy.ZERO
        )

        val STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ClientIntent::id)
    }
}