package dev.slne.surf.cloud.standalone.ktor.routes.punish.create.ban

import dev.slne.surf.cloud.standalone.ktor.types.ZonedDateTimeAsString
import dev.slne.surf.surfapi.core.api.service.UUIDAsString
import io.ktor.resources.*

@Resource("/ban")
class CreateBanPunishmentRoute(
    val id: Long,
    val punishmentId: String,
    val punishedUuid: UUIDAsString,
    val issuerUuid: UUIDAsString?,
    val reason: String?,
    val permanent: Boolean = false,
    val securityBan: Boolean = false,
    val raw: Boolean = false,

    val expirationDate: ZonedDateTimeAsString? = null,
    val punishmentDate: ZonedDateTimeAsString,
    val unpunished: Boolean = false,
    val unpunishedDate: ZonedDateTimeAsString? = null,
    val unpunisherUuid: UUIDAsString? = null,
)