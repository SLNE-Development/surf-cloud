package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentMuteImpl
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.MutePunishmentTable
import org.jetbrains.exposed.dao.id.EntityID

class MutePunishmentEntity(id: EntityID<Long>) :
    AbstractUnpunishableExpirablePunishmentEntity(id, MutePunishmentTable) {
    companion object : AuditableLongEntityClass<MutePunishmentEntity>(MutePunishmentTable)

    val notes by MutePunishmentEntity referrersOn MutePunishmentTable

    fun toApiObject() = PunishmentMuteImpl(
        id = id.value,
        punishmentId = punishmentId,
        punishedUuid =  punishedUuid,
        issuerUuid = issuerUuid,
        reason = reason,
        permanent = permanent,
        expirationDate = expirationDate,
        punishmentDate = createdAt,
        unpunished = unpunished,
        unpunishedDate = unpunishedDate,
        unpunisherUuid = unpunisherUuid
    )
}