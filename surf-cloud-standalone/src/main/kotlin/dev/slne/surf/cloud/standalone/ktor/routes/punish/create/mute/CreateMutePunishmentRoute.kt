package dev.slne.surf.cloud.standalone.ktor.routes.punish.create.mute

import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentMuteImpl
import dev.slne.surf.cloud.standalone.ktor.types.ZonedDateTimeAsString
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import io.ktor.resources.*
import java.time.ZonedDateTime

@Resource("/mute")
class CreateMutePunishmentRoute(
    val id: Long,
    val punishmentId: String,
    val punishedUuid: SerializableStringUUID,
    val issuerUuid: SerializableStringUUID?,
    val reason: String?,
    val permanent: Boolean = false,

    val expirationDate: ZonedDateTimeAsString? = null,
    val punishmentDate: ZonedDateTimeAsString = ZonedDateTime.now(),
    val unpunished: Boolean = false,
    val unpunishedDate: ZonedDateTimeAsString? = null,
    val unpunisherUuid: SerializableStringUUID? = null,
    val parent: CreateMutePunishmentRoute? = null
) {
    fun toApiObject(): PunishmentMuteImpl = PunishmentMuteImpl(
        id = id,
        punishmentId = punishmentId,
        punishedUuid = punishedUuid,
        issuerUuid = issuerUuid,
        reason = reason,
        permanent = permanent,
        expirationDate = expirationDate,
        punishmentDate = punishmentDate,
        unpunished = unpunished,
        unpunishedDate = unpunishedDate,
        unpunisherUuid = unpunisherUuid,
        parent = parent?.toApiObject()
    )
}