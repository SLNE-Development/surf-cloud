package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentMuteImpl
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.MutePunishmentTable
import org.jetbrains.exposed.dao.id.EntityID

class MutePunishmentEntity(id: EntityID<Long>) :
    AbstractUnpunishableExpirablePunishmentEntity<MutePunishmentEntity, MutePunishmentEntity.Companion>(
        id,
        MutePunishmentTable,
        MutePunishmentEntity
    ) {
    companion object : AuditableLongEntityClass<MutePunishmentEntity>(MutePunishmentTable)

    val notes by MutePunishmentEntity referrersOn MutePunishmentTable

    fun toApiObject(): PunishmentMuteImpl = PunishmentMuteImpl(
        id = id.value,
        punishmentId = punishmentId,
        punishedUuid = punishedPlayer.uuid,
        issuerUuid = issuerPlayer?.uuid,
        reason = reason,
        permanent = permanent,
        expirationDate = expirationDate,
        punishmentDate = createdAt,
        unpunished = unpunished,
        unpunishedDate = unpunishedDate,
        unpunisherUuid = unpunisherPlayer?.uuid,
        parent = parentPunishment?.toApiObject()
    )
}