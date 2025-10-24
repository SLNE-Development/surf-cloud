package dev.slne.surf.cloud.standalone.ktor.routes.punish.create.ban

import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentBanImpl
import dev.slne.surf.cloud.standalone.ktor.types.ZonedDateTimeAsString
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import io.ktor.resources.*

@Resource("/ban")
class CreateBanPunishmentRoute(
    val id: Long,
    val punishmentId: String,
    val punishedUuid: SerializableStringUUID,
    val issuerUuid: SerializableStringUUID?,
    val reason: String?,
    val permanent: Boolean = false,
    val securityBan: Boolean = false,
    val raw: Boolean = false,

    val expirationDate: ZonedDateTimeAsString? = null,
    val punishmentDate: ZonedDateTimeAsString,
    val unpunished: Boolean = false,
    val unpunishedDate: ZonedDateTimeAsString? = null,
    val unpunisherUuid: SerializableStringUUID? = null,
    val parent: CreateBanPunishmentRoute? = null,
) {
    fun toApiObject(): PunishmentBanImpl = PunishmentBanImpl(
        id = id,
        punishmentId = punishmentId,
        punishedUuid = punishedUuid,
        issuerUuid = issuerUuid,
        reason = reason,
        permanent = permanent,
        securityBan = securityBan,
        raw = raw,
        expirationDate = expirationDate,
        punishmentDate = punishmentDate,
        unpunished = unpunished,
        unpunishedDate = unpunishedDate,
        unpunisherUuid = unpunisherUuid,
        parent = parent?.toApiObject()
    )
}