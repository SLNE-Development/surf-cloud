package dev.slne.surf.cloud.standalone.ktor.routes.punish.create.warn

import dev.slne.surf.cloud.standalone.ktor.types.ZonedDateTimeAsString
import dev.slne.surf.surfapi.core.api.service.UUIDAsString
import io.ktor.resources.*
import java.time.ZonedDateTime

@Resource("/warn")
class CreateWarnPunishmentRoute(
    val id: Long,
    val punishmentId: String,
    val punishedUuid: UUIDAsString,
    val issuerUuid: UUIDAsString?,
    val reason: String?,

    val punishmentDate: ZonedDateTimeAsString = ZonedDateTime.now(),
)