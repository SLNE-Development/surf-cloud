package dev.slne.surf.cloud.api.common.player.whitelist

import dev.slne.surf.cloud.api.common.util.ByIdMap
import dev.slne.surf.cloud.api.common.util.IdRepresentable

enum class WhitelistStatus(override val id: Int) : IdRepresentable {
    NONE(0),
    ACTIVE(1),
    BLOCKED(2),
    UNKNOWN(3);

    companion object {
        val BY_ID = IdRepresentable.enumIdMap<WhitelistStatus>(ByIdMap.OutOfBoundsStrategy.LAST)
        val STREAM_CODEC = IdRepresentable.codec(BY_ID)
    }
}