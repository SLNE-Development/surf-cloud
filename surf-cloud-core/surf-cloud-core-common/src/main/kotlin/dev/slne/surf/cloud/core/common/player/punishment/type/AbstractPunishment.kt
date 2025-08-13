package dev.slne.surf.cloud.core.common.player.punishment.type

import dev.slne.surf.cloud.api.common.player.punishment.type.Punishment
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.cloud.core.common.player.CommonOfflineCloudPlayerImpl
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.util.*

@Serializable
sealed class AbstractPunishment() : Punishment {

    abstract override val parent: AbstractPunishment?
    abstract override val punishedUuid: UUID
    abstract override val punishmentDate: ZonedDateTime
    abstract override val punishmentId: String

    abstract val punishmentUrlReplacer: String
    override val adminPanelLink: String =
        "https://admin.slne.dev/core/user/$punishedUuid/$punishmentUrlReplacer/$punishmentId"

    final override fun punishedPlayer(): CommonOfflineCloudPlayerImpl =
        punishedUuid.toOfflineCloudPlayer() as CommonOfflineCloudPlayerImpl

    final override fun issuerPlayer() = issuerUuid.toOfflineCloudPlayer()
}