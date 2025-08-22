package dev.slne.surf.cloud.standalone.ktor.routes.punish.create.kick

import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentKickImpl
import dev.slne.surf.cloud.standalone.ktor.types.ZonedDateTimeAsString
import dev.slne.surf.surfapi.core.api.service.UUIDAsString
import kotlinx.serialization.Serializable

//@Resource("/kick")
@Serializable
class CreateKickPunishmentRoute(
    val id: Long,
    val punishmentId: String,
    val punishedUuid: UUIDAsString,
    val issuerUuid: UUIDAsString?,
    val reason: String?,
    val punishmentDate: ZonedDateTimeAsString,
    val parent: CreateKickPunishmentRoute? = null
) {
    fun toApiObject(): PunishmentKickImpl = PunishmentKickImpl(
        id = id,
        punishmentId = punishmentId,
        punishedUuid = punishedUuid,
        issuerUuid = issuerUuid,
        reason = reason,
        punishmentDate = punishmentDate,
        parent = parent?.toApiObject()
    )
}