package dev.slne.surf.cloud.api.common.player.punishment.type.mute

import dev.slne.surf.cloud.api.common.player.punishment.type.ExpirablePunishment
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import dev.slne.surf.cloud.api.common.player.punishment.type.UnpunishablePunishment

interface PunishmentMute: UnpunishablePunishment, ExpirablePunishment {
    override val type get() = PunishmentType.MUTE
}