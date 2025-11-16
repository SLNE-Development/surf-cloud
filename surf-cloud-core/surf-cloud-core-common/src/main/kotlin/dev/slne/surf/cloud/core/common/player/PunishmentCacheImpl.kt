package dev.slne.surf.cloud.core.common.player

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.player.punishment.PunishmentLoginValidation
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentBanImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentKickImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentMuteImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentWarnImpl

data class PunishmentCacheImpl(
    override val activeMutes: List<PunishmentMuteImpl>,
    override val activeBans: List<PunishmentBanImpl>,
    override val kicks: List<PunishmentKickImpl>,
    override val warnings: List<PunishmentWarnImpl>,
) : PunishmentLoginValidation.PunishmentCache {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            PunishmentMuteImpl.STREAM_CODEC
                .apply(ByteBufCodecs.list())
                .apply(ByteBufCodecs.makeImmutableList()),
            PunishmentCacheImpl::activeMutes,
            PunishmentBanImpl.STREAM_CODEC
                .apply(ByteBufCodecs.list())
                .apply(ByteBufCodecs.makeImmutableList()),
            PunishmentCacheImpl::activeBans,
            PunishmentKickImpl.STREAM_CODEC
                .apply(ByteBufCodecs.list())
                .apply(ByteBufCodecs.makeImmutableList()),
            PunishmentCacheImpl::kicks,
            PunishmentWarnImpl.STREAM_CODEC
                .apply(ByteBufCodecs.list())
                .apply(ByteBufCodecs.makeImmutableList()),
            PunishmentCacheImpl::warnings,
            ::PunishmentCacheImpl
        )
    }
}