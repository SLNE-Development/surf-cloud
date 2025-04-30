package dev.slne.surf.cloud.core.common.player.punishment.type

import dev.slne.surf.cloud.api.common.player.punishment.type.UnpunishablePunishment
import kotlinx.serialization.Serializable

@Serializable
sealed class AbstractUnpunishablePunishment() : AbstractPunishment(), UnpunishablePunishment
