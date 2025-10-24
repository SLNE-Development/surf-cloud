package dev.slne.surf.cloud.standalone.ktor.routes.punish.create.warn

import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentWarnImpl
import dev.slne.surf.cloud.standalone.ktor.types.ZonedDateTimeAsString
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import io.ktor.resources.*
import java.time.ZonedDateTime

@Resource("/warn")
class CreateWarnPunishmentRoute(
    val id: Long,
    val punishmentId: String,
    val punishedUuid: SerializableStringUUID,
    val issuerUuid: SerializableStringUUID?,
    val reason: String?,

    val punishmentDate: ZonedDateTimeAsString = ZonedDateTime.now(),
    val parent: CreateWarnPunishmentRoute? = null
) {
    fun toApiObject(): PunishmentWarnImpl = PunishmentWarnImpl(
        id = id,
        punishmentId = punishmentId,
        punishedUuid = punishedUuid,
        issuerUuid = issuerUuid,
        reason = reason,
        punishmentDate = punishmentDate,
        parent = parent?.toApiObject()
    )
}