package dev.slne.surf.cloud.standalone.ktor.routes.punish.update.mute

import dev.slne.surf.cloud.standalone.ktor.types.ZonedDateTimeAsString
import dev.slne.surf.surfapi.core.api.service.UUIDAsString
import io.ktor.resources.*
import java.time.ZonedDateTime

@Resource("/mute")
class UpdateMutePunishmentRoute(
    val id: Long,
    val punishmentId: String,
    val punishedUuid: UUIDAsString,
    val issuerUuid: UUIDAsString?,
    val reason: String?,
    val permanent: Boolean = false,

    val expirationDate: ZonedDateTimeAsString? = null,
    val punishmentDate: ZonedDateTimeAsString = ZonedDateTime.now(),
    val unpunished: Boolean = false,
    val unpunishedDate: ZonedDateTimeAsString? = null,
    val unpunisherUuid: UUIDAsString? = null,
)