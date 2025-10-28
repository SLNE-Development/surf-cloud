package dev.slne.surf.cloud.standalone.ktor.routes.punish.update.kick

import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentKickImpl
import dev.slne.surf.cloud.standalone.ktor.types.ZonedDateTimeAsString
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import io.ktor.resources.*

@Resource("/kick")
class UpdateKickPunishmentRoute(
    val id: Long,
    val punishmentId: String,
    val punishedUuid: SerializableStringUUID,
    val issuerUuid: SerializableStringUUID?,
    val reason: String?,
    val punishmentDate: ZonedDateTimeAsString,
    val parent: UpdateKickPunishmentRoute? = null
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