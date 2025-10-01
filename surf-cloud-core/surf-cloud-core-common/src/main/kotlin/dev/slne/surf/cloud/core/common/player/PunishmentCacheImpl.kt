package dev.slne.surf.cloud.core.common.player

import dev.slne.surf.cloud.api.common.player.punishment.PunishmentLoginValidation
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentBanImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentKickImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentMuteImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentWarnImpl
import kotlinx.serialization.Serializable

@Serializable
data class PunishmentCacheImpl(
    override val activeMutes: List<PunishmentMuteImpl>,
    override val activeBans: List<PunishmentBanImpl>,
    override val kicks: List<PunishmentKickImpl>,
    override val warnings: List<PunishmentWarnImpl>,
) : PunishmentLoginValidation.PunishmentCache