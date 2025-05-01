package dev.slne.surf.cloud.api.common.player.punishment.type.warn

import dev.slne.surf.cloud.api.common.player.punishment.type.Punishment
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType

interface PunishmentWarn: Punishment {
    override val type get() = PunishmentType.WARN
}