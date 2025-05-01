package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentKickImpl
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.KickPunishmentTable
import org.jetbrains.exposed.dao.id.EntityID

class KickPunishmentEntity(id: EntityID<Long>) : AbstractPunishmentEntity(id, KickPunishmentTable) {
    companion object: AuditableLongEntityClass<KickPunishmentEntity>(KickPunishmentTable)

    val notes by KickPunishmentEntity referrersOn KickPunishmentTable

    fun toApiObject() = PunishmentKickImpl(
        id = id.value,
        punishmentId = punishmentId,
        punishedUuid = punishedUuid,
        issuerUuid = issuerUuid,
        reason = reason,
        punishmentDate = createdAt
    )
}