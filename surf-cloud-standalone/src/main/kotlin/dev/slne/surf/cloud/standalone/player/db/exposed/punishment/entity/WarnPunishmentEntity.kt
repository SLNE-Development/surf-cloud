package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentWarnImpl
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.WarnPunishmentTable
import org.jetbrains.exposed.dao.id.EntityID

class WarnPunishmentEntity(id: EntityID<Long>) :
    AbstractPunishmentEntity<WarnPunishmentEntity, WarnPunishmentEntity.Companion>(
        id,
        WarnPunishmentTable,
        WarnPunishmentEntity
    ) {
    companion object : AuditableLongEntityClass<WarnPunishmentEntity>(WarnPunishmentTable)

    val notes by WarnPunishmentEntity referrersOn WarnPunishmentTable

    fun toApiObject(): PunishmentWarnImpl = PunishmentWarnImpl(
        id = id.value,
        punishmentId = punishmentId,
        punishedUuid = punishedPlayer.uuid,
        issuerUuid = issuerPlayer?.uuid,
        reason = reason,
        punishmentDate = createdAt,
        parent = parentPunishment?.toApiObject()
    )
}