package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentKickImpl
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.KickPunishmentTable
import org.jetbrains.exposed.dao.id.EntityID

class KickPunishmentEntity(id: EntityID<Long>) :
    AbstractPunishmentEntity<KickPunishmentEntity, KickPunishmentEntity.Companion>(
        id,
        KickPunishmentTable,
        KickPunishmentEntity
    ) {
    companion object : AuditableLongEntityClass<KickPunishmentEntity>(KickPunishmentTable)

    val notes by KickPunishmentEntity referrersOn KickPunishmentTable

    fun toApiObject(): PunishmentKickImpl = PunishmentKickImpl(
        id = id.value,
        punishmentId = punishmentId,
        punishedUuid = punishedPlayer.uuid,
        issuerUuid = issuerPlayer?.uuid,
        reason = reason,
        punishmentDate = createdAt,
        parent = parentPunishment?.toApiObject()
    )
}