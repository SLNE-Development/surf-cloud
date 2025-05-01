package dev.slne.surf.cloud.api.common.player.punishment.type.kick

import dev.slne.surf.cloud.api.common.player.punishment.type.Punishment
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType

interface PunishmentKick : Punishment {
    override val type get() = PunishmentType.KICK
}