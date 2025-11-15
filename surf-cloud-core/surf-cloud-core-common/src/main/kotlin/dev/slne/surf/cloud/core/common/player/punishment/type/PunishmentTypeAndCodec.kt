package dev.slne.surf.cloud.core.common.player.punishment.type

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import io.netty.buffer.ByteBuf

data class PunishmentTypeAndCodec(
    val type: PunishmentType,
    val codec: StreamCodec<ByteBuf, out AbstractPunishment>
)