package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentWarnImpl
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.KickPunishmentTable
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.MutePunishmentTable
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.WarnPunishmentTable
import org.jetbrains.exposed.dao.id.EntityID

class WarnPunishmentEntity(id: EntityID<Long>) : AbstractPunishmentEntity(id, WarnPunishmentTable) {
    companion object: AuditableLongEntityClass<WarnPunishmentEntity>(WarnPunishmentTable)

    val notes by WarnPunishmentEntity referrersOn WarnPunishmentTable

    fun toApiObject() = PunishmentWarnImpl(
        id = id.value,
        punishmentId = punishmentId,
        punishedUuid = punishedUuid,
        issuerUuid = issuerUuid,
        reason = reason,
        punishmentDate = createdAt
    )
}