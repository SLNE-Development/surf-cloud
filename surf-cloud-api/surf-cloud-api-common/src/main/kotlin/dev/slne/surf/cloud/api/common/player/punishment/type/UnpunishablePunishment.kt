package dev.slne.surf.cloud.api.common.player.punishment.type

import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import java.time.ZonedDateTime
import java.util.*

interface UnpunishablePunishment : Punishment {
    val unpunished: Boolean
    val unpunishedDate: ZonedDateTime?
    val unpunisherUuid: UUID?

    fun unpunisherPlayer() = unpunisherUuid.toOfflineCloudPlayer()
}