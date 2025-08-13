package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity

import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerEntity
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.AbstractUnpunishableExpirablePunishmentTable
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

abstract class AbstractUnpunishableExpirablePunishmentEntity<E : AbstractPunishmentEntity<E, C>, C : LongEntityClass<E>>(
    id: EntityID<Long>,
    table: AbstractUnpunishableExpirablePunishmentTable,
    entityClass: C
) : AbstractPunishmentEntity<E, C>(id, table, entityClass) {
    var unpunished by table.unpunished
    var unpunishedDate by table.unpunishedDate
    var unpunisherPlayer by CloudPlayerEntity optionalReferencedOn table.unpunisherPlayerId
    var expirationDate by table.expirationDate
    var permanent by table.permanent
}